package th.co.krungthaiaxa.api.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.JasperUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;

/**
 * Note: When design jrxml file, when compiling, if you get error jasper java.lang.NoClassDefFoundError: org/codehaus/groovy/control/CompilationFailedException, then please fix it like this:
 * Open jrxml, remove property language="groovy", that's it!!!
 *
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

    public void exportPdfFile(String reportXrmlClassPath, Object data, String destinationFilePath) {
        InputStream jsonInputStream = ObjectMapperUtil.toJsonInputStream(objectMapper, data);
        File destinationFile = new File(destinationFilePath);
        IOUtil.createParentFolderIfNecessary(destinationFilePath);
        JasperUtil.exportPdf(reportXrmlClassPath, jsonInputStream, destinationFile.getAbsolutePath());
    }
}
