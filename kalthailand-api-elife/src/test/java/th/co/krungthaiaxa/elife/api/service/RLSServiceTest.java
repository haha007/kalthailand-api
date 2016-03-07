package th.co.krungthaiaxa.elife.api.service;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.elife.api.KalApiApplication;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;
import th.co.krungthaiaxa.elife.api.repository.CollectionFileRepository;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class RLSServiceTest {
    @Inject
    private RLSService rlsService;
    @Inject
    private CollectionFileRepository collectionFileRepository;

    @Test
    @Ignore
    public void should_not_import_collection_file_when_file_not_valid_excel_file() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/graph.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_when_file_not_found() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/something.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_with_extra_column() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_extraColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_with_missing_sheet() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_missingSheet.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_with_missing_column() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_missingColumn.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_with_wrong_column_name() throws Exception {
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_wrongColumnName.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_not_import_collection_file_twice() throws Exception {
        rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        assertThatThrownBy(() -> rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_full.xls")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Ignore
    public void should_import_empty_collection() throws Exception {
        rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_empty.xls"));
        List<CollectionFile> collectionFiles = collectionFileRepository.findAll();
        assertThat(collectionFiles).hasSize(1);
        assertThat(collectionFiles.get(0).getLines()).hasSize(10);
    }

    @Test
    @Ignore
    public void should_import_collection_file_with_lines() throws Exception {
        rlsService.importCollectionFile(this.getClass().getResourceAsStream("/collectionFile_full.xls"));
        List<CollectionFile> collectionFiles = collectionFileRepository.findAll();
        assertThat(collectionFiles).hasSize(1);
        assertThat(collectionFiles.get(0).getLines()).hasSize(50);
    }

}
