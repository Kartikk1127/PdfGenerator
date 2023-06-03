package com.Kartikey.PdfGenerator.controller;

import com.Kartikey.PdfGenerator.model.InvoiceData;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer; //to render html content into pdf format

import java.io.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private TemplateEngine templateEngine; //to render html templates

    @PostMapping("/")
    public String generateInvoice(@RequestBody InvoiceData invoiceData) throws DocumentException {

        //generating html content using thymeleaf
        String htmlContent = generateHtmlContent(invoiceData); //using thymeleaf template engine

        //generating pdf out of html data
        byte[] pdfBytes = generatePdf(htmlContent);

        //save the pdf to local storage
        String filename = "C:/Users/sriva/OneDrive/Desktop/samplePdfs/invoice.pdf";
        savePdfToLocalFile(pdfBytes,filename);

        //return filename for downloading purpose
        return filename;
    }




    @GetMapping("/{filename}")
    public byte[] downloadInvoice(@PathVariable String filename)
    {
        //retrieve the pdf from local storage
        File file = new File(filename);
        byte[] pdfBytes = new byte[(int)file.length()];
        OutputStream os = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(pdfBytes);
        }
        catch(Exception e)
        {
            System.out.println("exception caught : " + e.getStackTrace());
        }

        //delete the temporary pdf file(optional)
//        file.delete();
        return pdfBytes;
    }

    //processing our html ttemplate and populating it with the invoice data
    private String generateHtmlContent(InvoiceData invoiceData) {
        Context context = new Context();
        context.setVariable("invoice",invoiceData);
        return templateEngine.process("invoice-template",context);
    }

    private byte[] generatePdf(String htmlContent) throws DocumentException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();
        return outputStream.toByteArray();
    }

    private void savePdfToLocalFile(byte[] pdfBytes, String filename) {

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(pdfBytes);
        }
        catch(Exception e)
        {
            System.out.println("file not found");
        }
    }
}
