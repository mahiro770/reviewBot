package com.mahiro.reviewbot.dto;

/** お気に入り登録/解除リクエスト */
public class FavoriteRequest {

    private boolean favorite;

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
