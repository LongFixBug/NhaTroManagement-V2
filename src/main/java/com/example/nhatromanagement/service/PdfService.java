package com.example.nhatromanagement.service;

import com.example.nhatromanagement.model.Bill;
import java.io.ByteArrayOutputStream;

public interface PdfService {
    ByteArrayOutputStream generateBillPdf(Bill bill) throws Exception;
}
