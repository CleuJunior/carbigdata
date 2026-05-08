package br.com.ctkd.client_consumer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "address-view")
public class AddressView extends View {
    @Indexed(unique = true)
    @Field("address_id")
    private String addressId;
    @Field("street_name")
    private String streetName;
    private String neighborhood;
    @Field("zip_code")
    private String zipCode;
    private String city;
    private String state;

}
