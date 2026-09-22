package br.com.facilita.apuracao.api;

import java.util.ArrayList;
import java.util.List;

public class ListarApuracoesResponse {

    private List<ApuracaoResponse> items = new ArrayList<ApuracaoResponse>();
    private long total;
    private long pendingCount;
    private long withAttachmentCount;
    private boolean nextPage;

    public ListarApuracoesResponse() {
    }

    public List<ApuracaoResponse> getItems() {
        return items;
    }

    public void setItems(List<ApuracaoResponse> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getWithAttachmentCount() {
        return withAttachmentCount;
    }

    public void setWithAttachmentCount(long withAttachmentCount) {
        this.withAttachmentCount = withAttachmentCount;
    }

    public boolean isNextPage() {
        return nextPage;
    }

    public void setNextPage(boolean nextPage) {
        this.nextPage = nextPage;
    }
}
