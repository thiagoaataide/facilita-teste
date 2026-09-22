package br.com.facilita.apuracao.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Página de snapshots retornada pelo adapter de consulta. */
public final class ApuracaoPage {

    private final List<ApuracaoSnapshot> items;
    private final long total;
    private final long pendingCount;
    private final long withAttachmentCount;
    private final boolean nextPage;

    public ApuracaoPage(List<ApuracaoSnapshot> items, long total, long pendingCount,
            long withAttachmentCount, boolean nextPage) {
        List<ApuracaoSnapshot> safeItems = items == null
                ? Collections.<ApuracaoSnapshot>emptyList() : items;
        this.items = Collections.unmodifiableList(new ArrayList<ApuracaoSnapshot>(safeItems));
        this.total = total;
        this.pendingCount = pendingCount;
        this.withAttachmentCount = withAttachmentCount;
        this.nextPage = nextPage;
    }

    public List<ApuracaoSnapshot> getItems() { return items; }
    public long getTotal() { return total; }
    public long getPendingCount() { return pendingCount; }
    public long getWithAttachmentCount() { return withAttachmentCount; }
    public boolean isNextPage() { return nextPage; }
}
