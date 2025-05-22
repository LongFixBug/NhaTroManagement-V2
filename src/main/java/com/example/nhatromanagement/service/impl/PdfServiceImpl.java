package com.example.nhatromanagement.service.impl;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfServiceImpl implements PdfService {

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;


    @Autowired
    public PdfServiceImpl(TemplateEngine templateEngine, MessageSource messageSource) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    @Override
    public ByteArrayOutputStream generateBillPdf(Bill bill) throws Exception {
        Locale locale = LocaleContextHolder.getLocale();
        Context context = new Context(locale);
        context.setVariable("bill", bill);
        context.setVariable("messageSource", messageSource); // Pass messageSource to template
        context.setVariable("locale", locale); // Pass locale to template

        String appTitle = messageSource.getMessage("app.title", null, locale);
        context.setVariable("appTitle", appTitle);

        // For formatting dates and numbers within the PDF template if needed
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", locale);
        context.setVariable("dateFormatter", dateFormatter);

        // Example of resolving a title message to pass to the context
        // The template can also resolve messages itself using #{...}
        context.setVariable("pdfTitle", messageSource.getMessage("bill.pdf.title",
                new Object[]{bill.getTenant().getName(), bill.getBillMonth(), bill.getBillYear()},
                locale));

        String htmlContent = templateEngine.process("bills/pdf_template", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        // It's good practice to set a base URL if your HTML references external resources like CSS files or images
        // For local resources, you might need to construct a file:/// URL
        // String baseUrl = Paths.get("src/main/resources/templates/").toUri().toURL().toString();
        // renderer.setDocumentFromString(htmlContent, baseUrl);
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();

        return outputStream;
    }
}
