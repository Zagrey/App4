package com.common.util;

import javax.persistence.*;


@Entity(name = "phonetype")
//@Table(name = "phonetype", schema = "public", catalog = "app")
public class PhonetypeEntity {
    @Id
    private String type;
    private String description;
}
