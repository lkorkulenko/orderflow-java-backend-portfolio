package dev.portfolio.orderflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.portfolio.orderflow.persistence.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private PurchaseOrderRepository orders;
    @Autowired private OrderLineRepository lines;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @BeforeEach void resetDatabase() {
        lines.deleteAllInBatch();
        orders.deleteAllInBatch();
    }

    private void seed(int count) {
        for (int i = 1; i <= count; i++) {
            PurchaseOrder order = new PurchaseOrder("EXAMPLE-" + i, Instant.parse("2026-01-01T00:00:00Z").plusSeconds(i));
            order.addLine("SKU-A", 2, new BigDecimal("12.50"));
            order.addLine("SKU-B", 1, new BigDecimal("4.25"));
            orders.save(order);
        }
        orders.flush();
    }

    private JsonNode query(String url) throws Exception {
        String body = mvc.perform(get(url)).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();
        return json.readTree(body);
    }

    @Test void optimizedListKeepsQueriesBoundedAndMatchesLegacyOutput() throws Exception {
        seed(25);
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        JsonNode slow = query("/api/orders/legacy?page=0&size=20");
        long legacyStatements = statistics.getPrepareStatementCount();

        statistics.clear();
        JsonNode fast = query("/api/orders?page=0&size=20");
        long optimizedStatements = statistics.getPrepareStatementCount();

        assertThat(fast).isEqualTo(slow);
        assertThat(legacyStatements).as("legacy N+1 statement count").isGreaterThanOrEqualTo(21L);
        assertThat(optimizedStatements).as("optimized statement count").isLessThanOrEqualTo(3L);
        System.out.printf("SQL statements for 20-order page: legacy=%d, optimized=%d%n",
                          legacyStatements, optimizedStatements);
    }

    @Test void createValidateConflictAndTransition() throws Exception {
        String request = """
            {"clientReference":"INV-2026-001","lines":[{"sku":"SKU-1","quantity":3,"unitPrice":12.50}]}
            """;
        String response = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(37.5))
            .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).get("id").asLong();

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isConflict());
        mvc.perform(patch("/api/orders/" + id + "/status")
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PROCESSING"));
        mvc.perform(patch("/api/orders/" + id + "/status")
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"NEW\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
            .content("{\"clientReference\":\"bad\",\"lines\":[{\"sku\":\"SKU\",\"quantity\":0,\"unitPrice\":1}]}"))
            .andExpect(status().isBadRequest());
    }

    @Test void rejectsValuesThatWouldExceedDatabaseColumnLimits() throws Exception {
        String tooLongSku = "X".repeat(81);
        String skuPayload = """
            {"clientReference":"SKU-TOO-LONG","lines":[{"sku":"%s","quantity":1,"unitPrice":1.00}]}
            """.formatted(tooLongSku);
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(skuPayload))
            .andExpect(status().isBadRequest());

        String excessiveScale = """
            {"clientReference":"MONEY-SCALE","lines":[{"sku":"SKU-1","quantity":1,"unitPrice":1.234}]}
            """;
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(excessiveScale))
            .andExpect(status().isBadRequest());

        String excessivePrecision = """
            {"clientReference":"MONEY-PRECISION","lines":[{"sku":"SKU-1","quantity":1,"unitPrice":10000000000.00}]}
            """;
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(excessivePrecision))
            .andExpect(status().isBadRequest());

        assertThat(orders.count()).isZero();
    }

    @Test void emptyPageAndMissingOrderAreHandled() throws Exception {
        mvc.perform(get("/api/orders")).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/orders/404")).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders?size=101")).andExpect(status().isBadRequest());
    }
}
