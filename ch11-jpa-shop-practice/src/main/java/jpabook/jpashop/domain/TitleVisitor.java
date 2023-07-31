package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Movie;

public class TitleVisitor implements Visitor {

    private String title;

    public String getTitle() {
        return title;
    }

    @Override
    public void visit(Album album) {
        this.title = album.toString();
    }

    @Override
    public void visit(Book book) {
        this.title = book.toString();
    }

    @Override
    public void visit(Movie movie) {
        this.title = movie.toString();
    }
}
