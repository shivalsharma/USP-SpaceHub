package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="system_settings") @Getter @Setter @NoArgsConstructor
public class SystemSetting {
    @Id @Column(name="setting_key", length=80) private String key;
    @Column(name="setting_value", nullable=false, length=500) private String value;
    @Column(nullable=false, length=500) private String description;
    public SystemSetting(String key,String value,String description){this.key=key;this.value=value;this.description=description;}
}
