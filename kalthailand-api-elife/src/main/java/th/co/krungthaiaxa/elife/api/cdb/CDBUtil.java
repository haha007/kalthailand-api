package th.co.krungthaiaxa.elife.api.cdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class CDBUtil {
    private final static Logger logger = LoggerFactory.getLogger(CDBUtil.class);

    @Value("${cdb.db.ip}")
    private String ip;
    @Value("${cdb.db.name}")
    private String dbName;
    @Value("${cdb.db.user}")
    private String user;
    @Value("${cdb.db.pass}")
    private String pass;

    private Connection conn;

    private void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://" + ip + ":1433;databaseName=" + dbName + ";user=" + user + ";password=" + pass);
            System.out.println("Connection successfully.");
        } catch (Exception e) {
            System.out.println("Failed connection.");
        }
    }

    private void close() {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println("Failed to close connection.");
        }
    }

    public Map<String, String> getExistingAgentCode(String idCard, String dateOfBirth) throws Exception {
        Map<String, String> outMap = null;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("[%1$s] .....", "getExistingAgentCode"));
            logger.debug(String.format("idCard is %1$s", idCard));
            logger.debug(String.format("dateOfBrith is %1$s", dateOfBirth));
        }
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            connect();
            ps = conn.prepareStatement("select top 1 pno, pagt1, pagt2 from lfkludta_lfppml where left(coalesce(pagt1,'0'),1) not in ('2','4') and left(coalesce(pagt2,'0'),1) not in ('2','4') and pterm = 0 and pstu in ('1','2','B') and '" + idCard + "' = case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 then ltrim(rtrim(coalesce(pownid,''))) else ltrim(rtrim(coalesce(pid,''))) end and " + dateOfBirth + " = case when ltrim(rtrim(coalesce(pownid,''))) <> '' and lpaydb <> 0 then lpaydb else pdob end order by pdoi desc");
            rs = ps.executeQuery();
            if (rs.next()) {
                outMap = new HashMap<>();
                outMap.put("pno", rs.getString("pno"));
                outMap.put("pagt1", rs.getString("pagt1"));
                outMap.put("pagt2", rs.getString("pagt2"));
                rs.close();
            }
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
        }
        rs = null;
        ps = null;
        close();
        return outMap;
    }

}
