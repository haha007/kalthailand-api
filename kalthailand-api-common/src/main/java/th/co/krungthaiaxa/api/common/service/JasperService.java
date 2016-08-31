package th.co.krungthaiaxa.api.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.JasperUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import javax.inject.Inject;
import java.io.InputStream;

/**
 * @author khoi.tran on 8/31/16.
 */
@Service
public class JasperService {
    private final ObjectMapper objectMapper;

    @Inject
    public JasperService(ObjectMapper objectMapper) {this.objectMapper = objectMapper;}

    public byte[] exportPdf(String reportXrmlClassPath, Object data) {
        InputStream jsonInputStream = ObjectMapperUtil.toJsonInputStream(objectMapper, data);
        return JasperUtil.exportPdf(reportXrmlClassPath, jsonInputStream);
    }

    public void exportPdfFile(String reportXrmlClassPath, Object data, String destinationFile) {
        InputStream jsonInputStream = ObjectMapperUtil.toJsonInputStream(objectMapper, data);
        JasperUtil.exportPdf(reportXrmlClassPath, jsonInputStream, destinationFile);
    }
}
