package com.cmbc.configserver.domain;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/30
 *         Time: 14:03
 */
public class Category {
    public static final Category EMPTY_MESSAGE = new Category();
    private int id;
    private String cell;
    private String resource;
    private String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", cell='" + cell + '\'' +
                ", resource='" + resource + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        if (id != category.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        return result;
    }
}
