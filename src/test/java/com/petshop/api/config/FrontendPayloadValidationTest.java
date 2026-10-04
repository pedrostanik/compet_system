package com.petshop.api.config;

import com.petshop.api.auth.filter.JwtFilter;
import com.petshop.api.packages.controller.PackController;
import com.petshop.api.packages.service.PackService;
import com.petshop.api.products.controller.ProductController;
import com.petshop.api.products.service.ProductService;
import com.petshop.api.protocols.controller.ProtocolController;
import com.petshop.api.protocols.service.ProtocolService;
import com.petshop.api.receipts.controller.ReceiptController;
import com.petshop.api.receipts.service.ReceiptService;
import com.petshop.api.schedule.controller.SchedulingController;
import com.petshop.api.schedule.service.SchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bean Validation must not reject what the web app legitimately sends. Each "accepts" test
 * reproduces a payload exactly as petshop-web builds it (empty strings, numbers as strings,
 * extra fields, zero prices, past dates…); each "rejects" test covers input that used to
 * reach the database or a NullPointerException.
 */
@WebMvcTest({ProductController.class, SchedulingController.class, ReceiptController.class,
        PackController.class, ProtocolController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class) // parse JSON exactly like production (trims strings, "" -> null)
class FrontendPayloadValidationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductService productService;
    @MockitoBean private SchedulingService schedulingService;
    @MockitoBean private ReceiptService receiptService;
    @MockitoBean private PackService packService;
    @MockitoBean private ProtocolService protocolService;
    @MockitoBean private JwtFilter jwtFilter;

    private ResultActions send(String method, String url, String json) throws Exception {
        var builder = "PUT".equals(method) ? put(url) : post(url);
        return mockMvc.perform(builder.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // --- Products (ProductForm: no client validation, numbers typed become strings) ---

    @Test
    void product_acceptsFormPayload_withBlankOptionalsAndStringNumbers() throws Exception {
        send("POST", "/api/products", """
                {"name":"Ração Premium","category":"ALI","animalTarget":"DOG","brand":"","unit":"UN",
                 "costPrice":"12.5","salePrice":"20","barcode":"","minStockQty":"","currentStockQty":"",
                 "shelfLocation":"","ncm":"2309.90.00","loose":false,"size":" ","color":""}""")
                .andExpect(status().isCreated());
    }

    @Test
    void product_rejectsEmptyForm() throws Exception {
        // Used to throw a NullPointerException in the SKU generator (500).
        send("POST", "/api/products", """
                {"name":"","category":"","animalTarget":"","unit":"UN","costPrice":"","salePrice":"","loose":false}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.category").exists())
                .andExpect(jsonPath("$.errors.costPrice").exists());
    }

    @Test
    void product_unknownEnumValue_isReportedOnTheField() throws Exception {
        send("POST", "/api/products", """
                {"name":"X","category":"XYZ","animalTarget":"DOG","unit":"UN","costPrice":"1","salePrice":"5","loose":false}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.category").value("valor inválido"));
    }

    @Test
    void receipt_textInNumberField_isReportedWithItsIndex() throws Exception {
        send("POST", "/api/receipts", """
                {"type":"PRODUCT","items":[{"description":"Ração","quantity":"abc","unitPrice":10}]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].quantity']").value("valor inválido"));
    }

    @Test
    void product_rejectsNegativePrice() throws Exception {
        send("POST", "/api/products", """
                {"name":"X","category":"ALI","animalTarget":"DOG","unit":"UN","costPrice":"-1","salePrice":"5","loose":false}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.costPrice").exists());
    }

    // --- Scheduling (SchedulingForm / FuturePackSchedulingsPanel) ---

    @Test
    void scheduling_acceptsPastTime_zeroPrice_emptyProtocols_noPackId() throws Exception {
        send("POST", "/api/scheduling", """
                {"customerId":3,"customerName":"Ana","petId":5,"petName":"Nox","schedulingObservations":"",
                 "time":"2026-01-10T09:00","isPackage":false,"protocolIds":[],"duration":60,"price":0}""")
                .andExpect(status().isCreated());
    }

    @Test
    void scheduling_acceptsPanelEdit_withNullIsPackageAndObservations() throws Exception {
        send("PUT", "/api/scheduling/7", """
                {"customerId":3,"customerName":"Ana","petId":5,"petName":"Nox","schedulingObservations":null,
                 "time":"2026-11-02T10:30","isPackage":null,"protocolIds":[1,2],"duration":60,
                 "price":33.333333333,"packId":null}""")
                .andExpect(status().isOk());
    }

    @Test
    void scheduling_rejectsClearedTime() throws Exception {
        // FuturePackSchedulingsPanel can send time "" if the input is cleared; that used to save a booking with no time.
        send("PUT", "/api/scheduling/7", """
                {"customerId":3,"time":"","duration":60}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.time").exists());
    }

    @Test
    void futureSchedules_acceptsFormPayload() throws Exception {
        send("POST", "/api/scheduling/future-schedules", """
                {"scheduling":{"customerId":3,"customerName":"Ana","petId":5,"petName":"Nox",
                  "time":"2026-11-02T10:30","isPackage":true,"protocolIds":[1],"duration":60,"price":45.5},
                 "time":"2026-11-02T10:30","packId":2}""")
                .andExpect(status().isCreated());
    }

    @Test
    void futureSchedules_validatesNestedScheduling() throws Exception {
        send("POST", "/api/scheduling/future-schedules", """
                {"scheduling":{"time":"2026-11-02T10:30"},"packId":2}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['scheduling.customerId']").exists());
    }

    // --- Receipts (ReceiptForm) ---

    @Test
    void receipt_acceptsFractionalQuantity_zeroDiscount_noProductId() throws Exception {
        send("POST", "/api/receipts", """
                {"type":"SERVICE","customerName":"","petName":"","observations":"","discount":0,
                 "items":[{"description":"Banho","quantity":0.5,"unitPrice":0}]}""")
                .andExpect(status().isCreated());
    }

    @Test
    void receipt_rejectsItemWithoutDescriptionOrQuantity() throws Exception {
        send("POST", "/api/receipts", """
                {"type":"PRODUCT","discount":0,"items":[{"description":"","quantity":0,"unitPrice":10}]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].description']").exists())
                .andExpect(jsonPath("$.errors['items[0].quantity']").exists());
    }

    @Test
    void receipt_rejectsMissingItems() throws Exception {
        // Used to throw a NullPointerException (500).
        send("POST", "/api/receipts", """
                {"type":"PRODUCT","discount":0}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    // --- Packs (PackForm) ---

    @Test
    void pack_acceptsFormPayload_withFractionalQuantity() throws Exception {
        // The quantity input does not enforce integers; Jackson truncates 1.5 to 1 as before.
        send("POST", "/api/pack", """
                {"name":"Banho semanal ","frequencia":"Semanal","protocols":[{"protocolId":1,"quantity":1.5}]}""")
                .andExpect(status().isCreated());
    }

    @Test
    void pack_rejectsUnknownFrequency() throws Exception {
        // Any other value made "generate future bookings" fail later with a 500.
        send("POST", "/api/pack", """
                {"name":"Pacote","frequencia":"Mensal","protocols":[{"protocolId":1,"quantity":1}]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.frequencia").exists());
    }

    // --- Protocols (ProtocolForm) ---

    @Test
    void protocol_acceptsNameWithEmptyDescription_andExtraPrice() throws Exception {
        send("POST", "/api/protocol", """
                {"name":"Tosa","description":"","price":null}""")
                .andExpect(status().isCreated());
    }

    @Test
    void validationMessages_followTheBrowserLanguage() throws Exception {
        mockMvc.perform(post("/api/protocol")
                        .header("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("não deve estar em branco"));
    }

    @Test
    void protocol_rejectsEmptyName() throws Exception {
        send("POST", "/api/protocol", """
                {"name":"","description":"x"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
