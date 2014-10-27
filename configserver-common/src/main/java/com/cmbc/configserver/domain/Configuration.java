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
	private Node node;
	
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
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuration other = (Configuration) obj;
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cell.equals(other.cell))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	@Override
    public String toString() {
        return "Configuration{" +
                "cell='" + cell + '\'' +
                ", resource='" + resource + '\'' +
                ", type='" + type + '\'' +
                ", node=" + node +
                '}';
    }
}
