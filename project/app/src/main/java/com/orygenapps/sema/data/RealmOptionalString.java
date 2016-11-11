package com.orygenapps.sema.data;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */

public class RealmOptionalString extends RealmObject {
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Required
    private String string;
}
