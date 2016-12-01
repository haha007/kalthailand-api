package th.co.krungthaiaxa.api.common.action;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author khoi.tran on 12/1/16.
 */
public abstract class ActionLoopByPage<E> {

    public void executeAllPages(int pageSize) {
        executeAllPages(pageSize, null);
    }

    /**
     * This method will stop if there's any exception are thrown, or the data in page is empty.
     *
     * @param pageSize pageSize of elements.
     * @param sort     sort for elements.
     */
    public void executeAllPages(int pageSize, Sort sort) {
        int page = 0;
        boolean isContinue = true;
        while (isContinue) {
            Pageable pageRequest;
            if (sort != null) {
                pageRequest = new PageRequest(page, pageSize, sort);
            } else {
                pageRequest = new PageRequest(page, pageSize);
            }
            List<E> pageData = executeEachPageData(pageRequest);
            isContinue = !pageData.isEmpty();
            page++;
        }
    }

    protected abstract List<E> executeEachPageData(Pageable pageRequest);
}
