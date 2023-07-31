package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Visitor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by holyeye on 2014. 3. 11..
 */

@Entity
@DiscriminatorValue("A")
public class Album extends Item {

    private String artist;
    private String etc;


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getEtc() {
        return etc;
    }

    public void setEtc(String etc) {
        this.etc = etc;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
