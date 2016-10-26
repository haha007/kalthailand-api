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
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.JasperException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author khoi.tran on 8/31/16.
 */
public class JasperUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(JasperUtil.class);

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
            LOGGER.debug("Exported file '{}'", destinationFile);
        } catch (JRException e) {
            throw new JasperException("Cannot read datasource from jsonInputStream. " + e.getMessage(), e);
        }
    }

    public static JasperPrint export(String jrxmlPath, Map<String, Object> parameters, JRDataSource dataSource) {
        JasperPrint jasperPrint = null;
        try (InputStream inStream = IOUtil.loadInputStreamFromClassPath(jrxmlPath);) {
            if (inStream == null) {
                String msg = String.format("Cannot find jrxmlPath '%s'", jrxmlPath);
                throw new JasperException(msg);
            }
            JasperDesign jasperDesign = JRXmlLoader.load(inStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            jasperReport.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        } catch (IOException | JRException jre) {
            String msg = String.format("Cannot create pdf from jrxmlPath '%s'. %s", jrxmlPath, jre.getMessage());
            throw new JasperException(msg, jre);
        }
        return jasperPrint;
    }

    public static byte[] exportPdfFromCompiledTemplate(String compiledJasperReportPath, ObjectMapper objectMapper, Object dataSource) {
        try {
            JsonDataSource jsonDataSource = toJsonDataSource(objectMapper, dataSource);
            JasperPrint jasperPrint = exportFromCompiledTemplate(compiledJasperReportPath, null, jsonDataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new JasperException("Cannot read datasource from jsonInputStream. " + e.getMessage(), e);
        }
    }

    public static JasperPrint exportFromCompiledTemplate(String compiledJasperReportPath, Map<String, Object> parameters, ObjectMapper objectMapper, Object dataSource) {
        JsonDataSource jsonDataSource = toJsonDataSource(objectMapper, dataSource);
        return exportFromCompiledTemplate(compiledJasperReportPath, parameters, jsonDataSource);
    }

    public static JasperPrint exportFromCompiledTemplate(String compiledJasperReportPath, Map<String, Object> parameters, JsonDataSource jsonDataSource) {
        try (InputStream compiledJasperReportInputStream = IOUtil.loadInputStreamFromClassPath(compiledJasperReportPath);) {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compiledJasperReportInputStream);
            jasperReport.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
            return JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);
        } catch (IOException | JRException e) {
            throw new JasperException("Cannot load jasper report template from: " + compiledJasperReportPath + ":%n " + e.getMessage(), e);
        }
    }

    private static JsonDataSource toJsonDataSource(ObjectMapper objectMapper, Object dataSource) {
        try (InputStream jsonInputStream = ObjectMapperUtil.toJsonInputStream(objectMapper, dataSource);) {
            return new JsonDataSource(jsonInputStream, null);
        } catch (IOException | JRException e) {
            throw new JasperException("Cannot create jsonDataSource form dataSource: " + e.getMessage(), e);
        }
    }
}
