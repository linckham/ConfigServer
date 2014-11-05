package com.cmbc.configserver.domain;
import com.cmbc.configserver.common.RemotingSerializable;
/**
 * the domain of the Configuration
 * @author tongchuan.lin<linckham@gmail.com>
 * @Modified jiang tao
 * 
 * @since 2014/10/17 3:01:22PM
 */
public class Configuration  extends RemotingSerializable{
	private String cell;
	private String resource;
	private String type;
    private String clientId;
    private int id;
    private int categoryId;
    private String content;

    private long createTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCell() {
		return cell;
	}
	public void setCell(String cell) {
		this.cell = cell;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration that = (Configuration) o;

        if (categoryId != that.categoryId) return false;
        if (id != that.id) return false;
        if (!cell.equals(that.cell)) return false;
        if (!clientId.equals(that.clientId)) return false;
        if (!content.equals(that.content)) return false;
        if (!resource.equals(that.resource)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cell.hashCode();
        result = 31 * result + resource.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + clientId.hashCode();
        result = 31 * result + id;
        result = 31 * result + categoryId;
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "cell='" + cell + '\'' +
                ", resource='" + resource + '\'' +
                ", type='" + type + '\'' +
                ", clientId='" + clientId + '\'' +
                ", id=" + id +
                ", categoryId=" + categoryId +
                ", content='" + content + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
