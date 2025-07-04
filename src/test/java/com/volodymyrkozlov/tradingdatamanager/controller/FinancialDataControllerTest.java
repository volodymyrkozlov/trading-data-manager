package com.volodymyrkozlov.tradingdatamanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volodymyrkozlov.tradingdatamanager.dto.FinancialDataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FinancialDataControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processesTradingPrices() throws Exception {
        // add 1st batches
        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_1_pln.json")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_1_uah.json")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_1_eur.json")))
                .andExpect(status().isOk());

        // add 2nd batches
        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_2_pln.json")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_2_uah.json")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/add_batch")
                        .contentType(APPLICATION_JSON)
                        .content(readBatchRequestData("spec/batch_2_eur.json")))
                .andExpect(status().isOk());

        // verifies stats for last 1e{k}
        var plnStats1e = objectMapper.readValue(mockMvc.perform(get("/stats/PLN/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(plnStats1e.last()).isEqualTo(61.4);
        assertThat(plnStats1e.min()).isEqualTo(0.27);
        assertThat(plnStats1e.max()).isEqualTo(77.51);
        assertThat(plnStats1e.avg()).isEqualTo(39.833999999999925);
        assertThat(plnStats1e.var()).isEqualTo(565.4533155555637);

        var uahStats1e = objectMapper.readValue(mockMvc.perform(get("/stats/UAH/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(uahStats1e.last()).isEqualTo(41.08);
        assertThat(uahStats1e.min()).isEqualTo(7.39);
        assertThat(uahStats1e.max()).isEqualTo(85.94);
        assertThat(uahStats1e.avg()).isEqualTo(45.910000000000124);
        assertThat(uahStats1e.var()).isEqualTo(574.7012222222098);

        var eurStats1e = objectMapper.readValue(mockMvc.perform(get("/stats/EUR/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);
        assertThat(eurStats1e.last()).isEqualTo(93.41);
        assertThat(eurStats1e.min()).isEqualTo(3.57);
        assertThat(eurStats1e.max()).isEqualTo(97.26);
        assertThat(eurStats1e.avg()).isEqualTo(56.898000000000046);
        assertThat(eurStats1e.var()).isEqualTo(1091.5071733333343);

        // verifies stats for last 2e{k}
        var plnStats2e = objectMapper.readValue(mockMvc.perform(get("/stats/PLN/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(plnStats2e.last()).isEqualTo(61.4);
        assertThat(plnStats2e.min()).isEqualTo(0.27);
        assertThat(plnStats2e.max()).isEqualTo(97.26);
        assertThat(plnStats2e.avg()).isEqualTo(46.462000000000046);
        assertThat(plnStats2e.var()).isEqualTo(824.229951515147);

        var uahStats2e = objectMapper.readValue(mockMvc.perform(get("/stats/UAH/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(uahStats2e.last()).isEqualTo(41.08);
        assertThat(uahStats2e.min()).isEqualTo(0.27);
        assertThat(uahStats2e.max()).isEqualTo(97.26);
        assertThat(uahStats2e.avg()).isEqualTo(46.46199999999999);
        assertThat(uahStats2e.var()).isEqualTo(824.2299515151524);

        var eurStats2e = objectMapper.readValue(mockMvc.perform(get("/stats/EUR/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);
        assertThat(eurStats2e.last()).isEqualTo(93.41);
        assertThat(eurStats2e.min()).isEqualTo(0.49);
        assertThat(eurStats2e.max()).isEqualTo(98.23);
        assertThat(eurStats2e.avg()).isEqualTo(51.36960000000003);
        assertThat(eurStats2e.var()).isEqualTo(951.8094907474729);

        // verifies stats for last 3e{k}
        var plnStats3e = objectMapper.readValue(mockMvc.perform(get("/stats/PLN/3"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(plnStats3e.last()).isEqualTo(61.4);
        assertThat(plnStats3e.min()).isEqualTo(0.27);
        assertThat(plnStats3e.max()).isEqualTo(98.23);
        assertThat(plnStats3e.avg()).isEqualTo(48.165312500000034);
        assertThat(plnStats3e.var()).isEqualTo(875.9349774023399);

        var uahStats3e = objectMapper.readValue(mockMvc.perform(get("/stats/UAH/3"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(uahStats3e.last()).isEqualTo(41.08);
        assertThat(uahStats3e.min()).isEqualTo(0.27);
        assertThat(uahStats3e.max()).isEqualTo(97.26);
        assertThat(uahStats3e.avg()).isEqualTo(46.461999999999996);
        assertThat(uahStats3e.var()).isEqualTo(815.9876520000007);

        var eurStats3e = objectMapper.readValue(mockMvc.perform(get("/stats/EUR/3"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);
        assertThat(eurStats3e.last()).isEqualTo(93.41);
        assertThat(eurStats3e.min()).isEqualTo(0.34);
        assertThat(eurStats3e.max()).isEqualTo(98.23);
        assertThat(eurStats3e.avg()).isEqualTo(49.51723529411766);
        assertThat(eurStats3e.var()).isEqualTo(954.6676400034594);

        // verifies stats for last 8e{k}
        var plnStats8e = objectMapper.readValue(mockMvc.perform(get("/stats/PLN/8"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(plnStats8e.last()).isEqualTo(61.4);
        assertThat(plnStats8e.min()).isEqualTo(0.27);
        assertThat(plnStats8e.max()).isEqualTo(98.23);
        assertThat(plnStats8e.avg()).isEqualTo(48.165312500000034);
        assertThat(plnStats8e.var()).isEqualTo(875.9349774023399);

        var uahStats8e = objectMapper.readValue(mockMvc.perform(get("/stats/UAH/8"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);

        assertThat(uahStats8e.last()).isEqualTo(41.08);
        assertThat(uahStats8e.min()).isEqualTo(0.27);
        assertThat(uahStats8e.max()).isEqualTo(97.26);
        assertThat(uahStats8e.avg()).isEqualTo(46.461999999999996);
        assertThat(uahStats8e.var()).isEqualTo(815.9876520000007);

        var eurStats8e = objectMapper.readValue(mockMvc.perform(get("/stats/EUR/8"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), FinancialDataResponse.class);
        assertThat(eurStats8e.last()).isEqualTo(93.41);
        assertThat(eurStats8e.min()).isEqualTo(0.34);
        assertThat(eurStats8e.max()).isEqualTo(98.23);
        assertThat(eurStats8e.avg()).isEqualTo(49.51723529411766);
        assertThat(eurStats8e.var()).isEqualTo(954.6676400034594);
    }

    private static String readBatchRequestData(String filename) throws IOException {
        return new String(new ClassPathResource(filename).getInputStream().readAllBytes());
    }
}
