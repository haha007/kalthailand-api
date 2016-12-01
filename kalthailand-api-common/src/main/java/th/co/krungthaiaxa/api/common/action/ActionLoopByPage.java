package th.co.krungthaiaxa.api.common.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author khoi.tran on 12/1/16.
 */
public abstract class ActionLoopByPage<E> {
    public static final Logger LOGGER = LoggerFactory.getLogger(ActionLoopByPage.class);

    private String name;

    public ActionLoopByPage() {
    }

    public ActionLoopByPage(String name) {
        this.name = name;
    }

    public void executeAllPages(int pageSize) {
        executeAllPages(pageSize, null);
    }

    /**
     * This method will stop if there's any exception are thrown, or the data in page is empty.
     *
     * @param pageSize pageSize of elements.
     * @param sort     sort for elements. Can be null.
     */
    public void executeAllPages(int pageSize, Sort sort) {
        int page = 0;
        boolean isContinue = true;
        while (isContinue) {
            Pageable pageRequest = new PageRequest(page, pageSize, sort);
            LOGGER.debug("Action '{}' execute page {}", name, pageRequest);
            List<E> pageData = executeEachPageData(pageRequest);
            isContinue = !pageData.isEmpty();
            page++;
        }
    }

    protected abstract List<E> executeEachPageData(Pageable pageRequest);
}
