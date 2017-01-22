package com.common.util;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "users")
//@Table(name = "users", schema = "public", catalog = "app")
public class UsersEntity {
    @Id
    private Long phone;
    private String name;
    private String phonetype;
    private String zip;
    private String email;
//    private PhonetypeEntity phoneTypeEntity;
//
//    @OneToOne
//    @JoinColumn(name = "phonetype", referencedColumnName = "type")
//    public PhonetypeEntity getPhoneTypeEntity() {
//        return phoneTypeEntity;
//    }

    private String address;
}
