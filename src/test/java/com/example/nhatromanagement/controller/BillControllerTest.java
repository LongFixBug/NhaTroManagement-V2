package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.service.BillService;
import com.example.nhatromanagement.service.SettingService;
import com.example.nhatromanagement.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BillControllerTest {

    private BillService billService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        billService = Mockito.mock(BillService.class);
        TenantService tenantService = Mockito.mock(TenantService.class);
        SettingService settingService = Mockito.mock(SettingService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0, String.class);
            if ("error.bill.notfound".equals(code)) {
                return "Bill not found";
            }
            if ("success.bill.deleted".equals(code)) {
                return "Bill deleted";
            }
            return code;
        });

        BillController controller = new BillController(billService, tenantService, messageSource, settingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deleteBill_acceptsPostAndRedirectsToTenantBills() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(42L);

        Bill bill = new Bill();
        bill.setId(7L);
        bill.setTenant(tenant);

        when(billService.getBillById(7L)).thenReturn(Optional.of(bill));

        mockMvc.perform(post("/bills/delete/7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bills/tenant/42"))
                .andExpect(flash().attribute("successMessage", "Bill deleted"));

        verify(billService).deleteBill(7L);
    }

    @Test
    void deleteBill_whenMissingRedirectsToBillsList() throws Exception {
        when(billService.getBillById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/bills/delete/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bills"))
                .andExpect(flash().attribute("errorMessage", "Bill not found"));
    }
}
