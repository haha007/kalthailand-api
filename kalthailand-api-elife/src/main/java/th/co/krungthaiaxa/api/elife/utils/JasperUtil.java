package th.co.krungthaiaxa.api.elife.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.JasperException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author khoi.tran on 8/31/16.
 */
public class JasperUtil {
    public static final Logger logger = LoggerFactory.getLogger(JasperUtil.class);

    ObjectMapper objectMapper;

    public static byte[] exportPdf(String jrxmlPath, InputStream jsonDataSourceInputSteam) {
        try {
            JsonDataSource jsonDataSource = new JsonDataSource(jsonDataSourceInputSteam, null);
            JasperPrint jasperPrint = export(jrxmlPath, null, jsonDataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new JasperException("Cannot read datasource from jsonInputStream. " + e.getMessage(), e);
        }
    }

    public static void exportPdf(String jrxmlPath, InputStream jsonDataSourceInputSteam, String destinationFile) {
        try {
            JsonDataSource jsonDataSource = new JsonDataSource(jsonDataSourceInputSteam, null);
            JasperPrint jasperPrint = export(jrxmlPath, null, jsonDataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, destinationFile);
            logger.debug("Exported file '{}'", destinationFile);
        } catch (JRException e) {
            throw new JasperException("Cannot read datasource from jsonInputStream. " + e.getMessage(), e);
        }
    }

    public static JasperPrint export(String jrxmlPath, Map<String, Object> parameters, JRDataSource dataSource) {
        JasperPrint jasperPrint = null;

        InputStream inStream = IOUtil.loadInputStreamFileInClassPath(jrxmlPath);
        if (inStream == null) {
            String msg = String.format("Cannot find jrxmlPath '%s'", jrxmlPath);
            throw new JasperException(msg);
        }
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(inStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            jasperReport.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        } catch (JRException jre) {
            String msg = String.format("Cannot create pdf from jrxmlPath '%s'. %s", jrxmlPath, jre.getMessage());
            throw new JasperException(msg, jre);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    String msg = String.format("Error closing stream from jrxmlPath '%s'. %s", jrxmlPath, e.getMessage());
                    logger.error(msg, e);
                }
            }
        }

        return jasperPrint;
    }
}
