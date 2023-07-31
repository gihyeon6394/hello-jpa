package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Movie;

public interface Visitor {
    void visit(Album album);

    void visit(Book book);

    void visit(Movie movie);
}

