package com.intita.wschat.annotations;

import java.lang.annotation.*;

/**
 * Created by roma on 14.06.17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ServerAccess {
}
