package com.codekutter.common.stores.model;

import com.codekutter.common.Context;
import com.codekutter.common.model.CopyException;
import com.codekutter.common.model.IEntity;
import com.codekutter.common.model.ValidationExceptions;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tb_customer")
public class Customer implements IEntity<String> {
    @Id
    @Column(name = "customer_id")
    private String id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    @Column(name = "email_id")
    private String emailId;
    @Column(name = "phone_no")
    private String phoneNumber;

    @Override
    public String getKey() {
        return id;
    }

    @Override
    public int compare(String key) {
        return id.compareTo(key);
    }

    @Override
    public IEntity<String> copyChanges(IEntity<String> source, Context context) throws CopyException {
        return null;
    }

    @Override
    public IEntity<String> clone(Context context) throws CopyException {
        return null;
    }

    @Override
    public void validate() throws ValidationExceptions {

    }
}