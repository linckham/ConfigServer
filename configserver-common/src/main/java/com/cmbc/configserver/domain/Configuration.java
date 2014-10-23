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
    public String toString() {
        return "Configuration{" +
                "cell='" + cell + '\'' +
                ", resource='" + resource + '\'' +
                ", type='" + type + '\'' +
                ", node=" + node +
                '}';
    }
}
