package com.wildcodeschool.blogJava.repository;

import java.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wildcodeschool.blogJava.config.AppConfig;
import com.wildcodeschool.blogJava.dao.UserDao;
import com.wildcodeschool.blogJava.dao.ArticleDao;
import com.wildcodeschool.blogJava.dao.TagDao;
import com.wildcodeschool.blogJava.model.Article;
import com.wildcodeschool.blogJava.model.Tag;
import com.wildcodeschool.blogJava.model.User;
import com.wildcodeschool.blogJava.util.JdbcUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.text.SimpleDateFormat;

@Repository
public class ArticleRepository implements ArticleDao {

    @Autowired
    private AppConfig config;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TagDao tagDao;

    @Override
    public List<Article> findAll() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JdbcUtils.getConnection(config.mysql);
            statement = connection.prepareStatement("SELECT id, title, content, image, published, id_user  "
                    + "  FROM article "
                    + "  ORDER BY published DESC ; ");
            resultSet = statement.executeQuery();

            List<Article> articles = new ArrayList<>();

            while (resultSet.next()) {

                // Read the article
                Long id = resultSet.getLong("id");
                String title = resultSet.getString("title");
                String content = resultSet.getString("content");
                String image = resultSet.getString("image");
                Date published = resultSet.getDate("published");
                Long id_user = resultSet.getLong("id_user");

                // Read the article's author
                User user = new User();
                user = userDao.findById(id_user);

                // Read the tag list of the article
                List<Tag> tags = new ArrayList<>();
                tags = tagDao.findAllInArticle(id);

                articles.add(new Article(id, title, content, image, published, user, tags));

            }

            return articles;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }
        return null;
    }

    @Override
    public Article findById(Long id) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JdbcUtils.getConnection(config.mysql);
            statement = connection.prepareStatement("SELECT id, title, content, image, published, id_user "
                    + " FROM article "
                    + " WHERE id = ? ");
            statement.setLong(1, id);
            resultSet = statement.executeQuery();

            Article article = new Article();

            if (resultSet.next()) {

                // Read the article
                String title = resultSet.getString("title");
                String content = resultSet.getString("content");
                String image = resultSet.getString("image");
                Date published = resultSet.getDate("published");
                Long id_user = resultSet.getLong("id_user");

                // Read the article's author
                User user = new User();
                user = userDao.findById(id_user);


                // Read the tag list of the article
                List<Tag> tags = new ArrayList<>();
                tags = tagDao.findAllInArticle(id);

                article.setId(id);
                article.setTitle(title);
                article.setContent(content);
                article.setImage(image);
                article.setPublished(published);
                article.setUser(user);
                article.setListTag(tags);
            }

            return article;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }

        return null;
    }

    @Override
    public void deleteById(Long id) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JdbcUtils.getConnection(config.mysql);
            statement = connection.prepareStatement(
                    "DELETE FROM article WHERE id = ?"
            );
            statement.setLong(1, id);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("failed to delete data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }

    }

    @Override
    public Article create(Article article) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {

            connection = JdbcUtils.getConnection(config.mysql);
            statement = connection.prepareStatement(
                    "INSERT INTO article (content, image, published, id_user) VALUES (?, ?, ?, ?)",
                    statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, article.getTitle());
            statement.setString(2, article.getContent());
            statement.setString(3, article.getImage());
            //TODO
            //statement.setDate(4, (java.util.Date) article.getPublished()); //convert from java.sql.Date to java.util.Date
            //statement.setDate(4, article.getPublished());
            statement.setLong(5, article.getUser().getId());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("failed to insert data into article");
            }

            generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                Long id = generatedKeys.getLong(1);
                article.setId(id);
                return article;
            } else {
                throw new SQLException("failed to get inserted id of article");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(generatedKeys);
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }

        return null;
    }

    @Override
    public Article update(Article article) {

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = JdbcUtils.getConnection(config.mysql);
            statement = connection.prepareStatement(
                    "UPDATE article SET content=?, image=?, published=?, id_user=? WHERE id=?"
            );
            statement.setString(1, article.getContent());
            statement.setString(2, article.getImage());
            //TODO
            statement.setDate(3, (java.sql.Date) article.getPublished()); //convert from java.util.Date to java.sql.Date
            statement.setLong(4, article.getUser().getId());
            statement.setLong(5, article.getId());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("failed to update data into article");
            }
            return article;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
        }

        return null;
    }

}