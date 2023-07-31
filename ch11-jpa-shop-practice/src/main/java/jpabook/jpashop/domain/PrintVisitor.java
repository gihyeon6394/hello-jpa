package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Movie;

public class PrintVisitor implements Visitor {

    @Override
    public void visit(Album album) {
        System.out.println("album = " + album);
    }

    @Override
    public void visit(Book book) {
        System.out.println("book = " + book);
    }

    @Override
    public void visit(Movie movie) {
        System.out.println("movie = " + movie);
    }
}
