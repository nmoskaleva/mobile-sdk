package msdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CredentialOffer {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "pwdid")
    public String pwDid;

    @ColumnInfo(name = "threadId")
    public String threadId;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "attachConnection")
    public String attachConnection;

    @ColumnInfo(name = "attachConnectionName")
    public String attachConnectionName;

    @ColumnInfo(name = "attachConnectionLogo")
    public String attachConnectionLogo;

}
