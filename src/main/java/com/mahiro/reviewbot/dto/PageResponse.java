package com.mahiro.reviewbot.dto;

import java.util.List;

/** 一覧系エンドポイントで使う共通のページングレスポンス */
public class PageResponse<T> {

    private List<T> items;
    private int total;
    private int limit;
    private int offset;
    private boolean hasMore;

    public static <T> PageResponse<T> of(List<T> items, int total, int limit, int offset) {
        PageResponse<T> res = new PageResponse<>();
        res.items = items;
        res.total = total;
        res.limit = limit;
        res.offset = offset;
        res.hasMore = offset + items.size() < total;
        return res;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
