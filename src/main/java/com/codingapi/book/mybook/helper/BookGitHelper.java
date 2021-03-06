package com.codingapi.book.mybook.helper;

import com.codingapi.book.mybook.config.MyBookConfig;
import com.codingapi.book.mybook.model.Book;
import com.codingapi.book.mybook.model.Catalog;
import com.codingapi.book.mybook.utils.MyFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lorne
 * @date 2018/9/14
 * @description
 */
@Component
@Slf4j
public class BookGitHelper {

    @Autowired
    private MyBookConfig myBookConfig;

    @Autowired
    private GitHelper gitHelper;


    private boolean gitHasExist(){
        File file = new File(myBookConfig.getGitSavePath());
        if(file.isDirectory()) {
            for(String name:file.list()){
                if(name.equalsIgnoreCase(".git")){
                    return true;
                }
            }
        }
        return false;
    }



    public void hadReady() {
        if(!gitHasExist()){
            gitHelper.download();
        }
    }

    public Catalog loadCatalog(){
        hadReady();
        String path = "/";
        return loadCatalogTree(path);
    }


    private String[] fileList(String path) {
        File file = new File(myBookConfig.getGitSavePath()+path);
        return file.list();
    }

    private String getGitTitle(String filePath) {
        File file = new File(myBookConfig.getGitSavePath()+filePath);
        String line =  MyFileUtils.readOneLine(file);
        line = line.replaceAll("#","");
        line = line.trim();
        return line;
    }


    private Catalog loadCatalogTree(String path){
        Catalog catalog = new Catalog();
        catalog.setPath(path);
        catalog.defaultTitle();
        List<Catalog> catalogs = new ArrayList<>();
        String[] files = fileList(path);
        for(String file:files){
            //过滤.git
            if(file.endsWith(".git")) {
                continue;
            }
            //加入md文件
            if(file.endsWith("md")) {
                String title = getGitTitle(path+file);
                catalogs.add(new Catalog(title,path+file));
            }else {
                File directoryFile = new File(myBookConfig.getGitSavePath()+ path+file);
                if (directoryFile.isDirectory()) {
                    Catalog node = loadCatalogTree(path+file+"/");
                    if(!node.hasNull()) {
                        catalogs.add(node);
                    }
                }
            }
        }
        catalog.setList(catalogs);
        return catalog;
    }

    public Book getIndexBook() {
        Book book = getContentBook("index.md");
        if("no data".equals(book.getContent())){
            book = getContentBook("readme.md");
        }
        if("no data".equals(book.getContent())){
            book = getContentBook("README.md");
        }
        return book;
    }



    public Book getContentBook(String path){
        Book book = new Book();
        book.setContent("no data");
        book.setTitle("book");
        book.setPath(path);
        File indexFile = new File(myBookConfig.getGitSavePath()+"/"+ path);
        if(indexFile.exists()){

            if(indexFile.isDirectory()){
                String fileNames[] = indexFile.list();
                for(String name:fileNames){
                    if(name.endsWith("md")){
                        indexFile = new File(myBookConfig.getGitSavePath()+"/"+ path+"/"+name);
                        book.setPath(path+"/"+name);
                        break;
                    }
                }
            }

            if(indexFile.isFile()) {
                try {
                    String content = FileUtils.readFileToString(indexFile, "utf8");
                    book.setContent(content);
                    book.setTitle(getContentTitle(book.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return book;
    }


    private String getContentTitle(String path){
        File indexFile = new File(myBookConfig.getGitSavePath()+"/"+ path);
        if(indexFile.exists()){
            String title = MyFileUtils.readOneLine(indexFile);
            if(title!=null){
                title = title.replaceAll("#","");
                title = title.trim();
            }
            return title;
        }
        return "no data";
    }
}
