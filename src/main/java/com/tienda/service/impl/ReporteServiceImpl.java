package com.tienda.service.impl;

import com.tienda.service.ReporteService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service

public class ReporteServiceImpl implements ReporteService {

    @Autowired
    DataSource dataSource;

    @Override
    public ResponseEntity<Resource> generaReporte(
            String reporte,
            Map<String, Object> parametros, String tipo) throws IOException {
        try {
            String estilo;
            if (tipo.equals("vPdf")) {
                estilo = "inline; ";
            } else {
                estilo = "attachment; ";
            }
            String reportePath = "reportes";

            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            ClassPathResource fuente = new ClassPathResource(
                    reportePath
                    + File.separator
                    + reporte
                    + ".jasper"
            );

            InputStream elReporte = fuente.getInputStream();

            var reportJasper = JasperFillManager.fillReport(elReporte, parametros, dataSource.getConnection());

            MediaType mediaType = null;

            String archivoSalida = "";

            byte[] data;

            switch (tipo) {
                case "Pdf", "vPdf" -> {
                    JasperExportManager.exportReportToPdfStream(reportJasper, salida);

                    mediaType = MediaType.APPLICATION_PDF;
                    archivoSalida = reporte + ".pdf";
                }
            }

            data = salida.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition", estilo + "filename= \"" + archivoSalida + "\"");

            return ResponseEntity.ok().headers(headers).contentType(mediaType).body(new InputStreamResource(new ByteArrayInputStream(data)));

        } catch (SQLException | JRException e) {
            e.printStackTrace();
        }

        return null;
    }

}
