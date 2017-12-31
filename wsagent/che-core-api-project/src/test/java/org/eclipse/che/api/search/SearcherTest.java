package org.eclipse.che.api.search;
/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.impl.RootAwarePathTransformer;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.api.search.server.Searcher;
import org.eclipse.che.api.search.server.impl.LuceneSearcher;
import org.eclipse.che.commons.lang.IoUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("Duplicates")
public class SearcherTest {
  private static final String[] TEST_CONTENT = {
    "Apollo set several major human spaceflight milestones",
    "Maybe you should think twice",
    "To be or not to be beeeee lambergeeene",
    "In early 1961, direct ascent was generally the mission mode in favor at NASA",
    "Time to think"
  };

  File indexDirectory;
  File workspaceStorage;
  Set<PathMatcher> excludePatterns;
  Searcher searcher;
  RootAwarePathTransformer pathTransformer;
  ContentBuilder contentBuilder;

  @BeforeMethod
  public void setUp() throws Exception {
    indexDirectory = Files.createTempDir();
    workspaceStorage = Files.createTempDir();
    excludePatterns = Collections.emptySet();
    //    File targetDir =
    //        new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath())
    //            .getParentFile();
    //    indexDirectory = new File(targetDir, NameGenerator.generate("index-", 4));
    //    assertTrue(indexDirectory.mkdir());

    //    filter = mock(VirtualFileFilter.class);
    //    when(filter.accept(any(VirtualFile.class))).thenReturn(false);
    //
    //    closeCallback = mock(AbstractLuceneSearcherProvider.CloseCallback.class);

    pathTransformer = new RootAwarePathTransformer(workspaceStorage);
    searcher =
        new LuceneSearcher(excludePatterns, indexDirectory, workspaceStorage, pathTransformer);
    contentBuilder = new ContentBuilder(workspaceStorage.toPath());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IoUtil.deleteRecursive(indexDirectory);
    IoUtil.deleteRecursive(workspaceStorage);
  }

  @Test
  public void shouldBeAbleToFindSingleFile() throws Exception {

    // given
    contentBuilder.createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[1]);
    // when
    searcher.add(contentBuilder.getLastUpdatedFile());
    // then
    assertFind(new QueryExpression().setText("should"), "/aaa/aaa.txt");
  }

  @Test
  public void shouldBeAbleToFindTwoFilesAddedAsSingleDirectory() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[1]);
    // when
    searcher.add(contentBuilder.getCurrentFolder());
    // then
    assertFind(new QueryExpression().setText("be"), "/folder/xxx.txt");
    assertFind(new QueryExpression().setText("should"), "/folder/zzz.txt");
  }

  @Test
  public void shouldBeAbleToUpdateSingleFile() throws Exception {
    // given
    contentBuilder.createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getLastUpdatedFile());
    assertEmptyResult(new QueryExpression().setText("should"));
    // when
    contentBuilder.createFile("aaa.txt", TEST_CONTENT[1]);
    searcher.add(contentBuilder.getLastUpdatedFile());
    // then
    assertFind(new QueryExpression().setText("should"), "/aaa/aaa.txt");
  }


  //
  //  @Test
  //  public void deletesSingleFileFromIndex() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile file =
  //        virtualFileSystem.getRoot().createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertEquals(newArrayList(file.getPath().toString()), paths);
  //
  //    searcher.delete(file.getPath().toString(), file.isFile());
  //
  //    paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertTrue(paths.isEmpty());
  //  }
  //
  //  @Test
  //  public void deletesFileTreeFromIndex() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[1]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt"), paths);
  //    paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
  //    assertEquals(newArrayList("/folder/zzz.txt"), paths);
  //
  //    searcher.delete("/folder", false);
  //
  //    paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertTrue(paths.isEmpty());
  //    paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
  //    assertTrue(paths.isEmpty());
  //  }
  //
  //  @Test
  //  public void searchesByWordFragment() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[0]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths = searcher.search(new
  // QueryExpression().setText("*stone*")).getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt"), paths);
  //  }
  //
  //  @Test
  //  public void searchesByTextAndFileName() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths =
  //        searcher.search(new QueryExpression().setText("be").setName("xxx.txt")).getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt"), paths);
  //  }
  //
  //  @Test
  //  public void searchesByFullTextAndFileName() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    SearchResult result =
  //        searcher.search(
  //            new QueryExpression().setText("*be*").setName("xxx.txt").setIncludePositions(true));
  //    List<String> paths = result.getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt"), paths);
  //    assertEquals(result.getResults().get(0).getData().size(), 4);
  //  }
  //
  //  @Test
  //  public void searchesByFullTextAndFileName2() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[4]);
  //    searcher.init(virtualFileSystem);
  //
  //    SearchResult result =
  //        searcher.search(new QueryExpression().setText("*to*").setIncludePositions(true));
  //    List<String> paths = result.getFilePaths();
  //    assertEquals(paths.size(), 2);
  //    assertEquals(result.getResults().get(0).getData().size(), 2);
  //  }
  //
  //  @DataProvider
  //  public Object[][] searchByName() {
  //    return new Object[][] {
  //      {"sameName.txt", "sameName.txt"},
  //      {"notCaseSensitive.txt", "notcasesensitive.txt"},
  //      {"fullName.txt", "full*"},
  //      {"file name.txt", "file name"},
  //      {"prefixFileName.txt", "prefixF*"},
  //      {"name.with.dot.txt", "name.With.Dot.txt"},
  //    };
  //  }
  //
  //  @Test(dataProvider = "searchByName")
  //  public void searchFileByName(String fileName, String searchedFileName) throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("parent/child");
  //    VirtualFile folder2 = virtualFileSystem.getRoot().createFolder("folder2");
  //    folder.createFile(NameGenerator.generate(null, 10), TEST_CONTENT[3]);
  //    folder.createFile(fileName, TEST_CONTENT[2]);
  //    folder.createFile(NameGenerator.generate(null, 10), TEST_CONTENT[1]);
  //    folder2.createFile(NameGenerator.generate(null, 10), TEST_CONTENT[2]);
  //    folder2.createFile(NameGenerator.generate(null, 10), TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths =
  //        searcher.search(new QueryExpression().setName(searchedFileName)).getFilePaths();
  //    assertEquals(newArrayList("/parent/child/" + fileName), paths);
  //  }
  //
  //  @Test
  //  public void searchesByTextAndPath() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder1 = virtualFileSystem.getRoot().createFolder("folder1/a/b");
  //    VirtualFile folder2 = virtualFileSystem.getRoot().createFolder("folder2");
  //    folder1.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder2.createFile("zzz.txt", TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths =
  //        searcher.search(new QueryExpression().setText("be").setPath("/folder1")).getFilePaths();
  //    assertEquals(newArrayList("/folder1/a/b/xxx.txt"), paths);
  //  }
  //
  //  @Test
  //  public void searchesByTextAndPathAndFileName() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder1 = virtualFileSystem.getRoot().createFolder("folder1/a/b");
  //    VirtualFile folder2 = virtualFileSystem.getRoot().createFolder("folder2/a/b");
  //    folder1.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder1.createFile("yyy.txt", TEST_CONTENT[2]);
  //    folder2.createFile("zzz.txt", TEST_CONTENT[2]);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths =
  //        searcher
  //            .search(new QueryExpression().setText("be").setPath("/folder1").setName("xxx.txt"))
  //            .getFilePaths();
  //    assertEquals(newArrayList("/folder1/a/b/xxx.txt"), paths);
  //  }
  //
  //  @Test
  //  public void closesLuceneIndexWriterWhenSearcherClosed() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    searcher.init(virtualFileSystem);
  //
  //    searcher.close();
  //
  //    assertTrue(searcher.isClosed());
  //    assertFalse(searcher.getIndexWriter().isOpen());
  //  }
  //
  //  @Test
  //  public void notifiesCallbackWhenSearcherClosed() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    searcher.init(virtualFileSystem);
  //
  //    searcher.close();
  //    verify(closeCallback).onClose();
  //  }
  //
  //  @Test
  //  public void excludesFilesFromIndexWithFilter() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("yyy.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[2]);
  //
  //    when(filter.accept(withName("yyy.txt"))).thenReturn(true);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt", "/folder/zzz.txt"), paths);
  //  }
  //
  //  @Test
  //  public void limitsNumberOfSearchResultsWhenMaxItemIsSet() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    for (int i = 0; i < 125; i++) {
  //      virtualFileSystem
  //          .getRoot()
  //          .createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
  //    }
  //    searcher.init(virtualFileSystem);
  //
  //    SearchResult result = searcher.search(new
  // QueryExpression().setText("mission").setMaxItems(5));
  //
  //    assertEquals(25, result.getTotalHits());
  //    assertEquals(5, result.getFilePaths().size());
  //  }
  //
  //  @Test
  //  public void generatesQueryExpressionForRetrievingNextPageOfResults() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    for (int i = 0; i < 125; i++) {
  //      virtualFileSystem
  //          .getRoot()
  //          .createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
  //    }
  //    searcher.init(virtualFileSystem);
  //
  //    SearchResult result =
  //        searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(7));
  //
  //    assertEquals(result.getTotalHits(), 25);
  //
  //    Optional<QueryExpression> optionalNextPageQueryExpression =
  // result.getNextPageQueryExpression();
  //    assertTrue(optionalNextPageQueryExpression.isPresent());
  //    QueryExpression nextPageQueryExpression = optionalNextPageQueryExpression.get();
  //    assertEquals("spaceflight", nextPageQueryExpression.getText());
  //    assertEquals(7, nextPageQueryExpression.getSkipCount());
  //    assertEquals(7, nextPageQueryExpression.getMaxItems());
  //  }
  //
  //  @Test
  //  public void retrievesSearchResultWithPages() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    for (int i = 0; i < 125; i++) {
  //      virtualFileSystem
  //          .getRoot()
  //          .createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
  //    }
  //    searcher.init(virtualFileSystem);
  //
  //    SearchResult firstPage =
  //        searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(8));
  //    assertEquals(firstPage.getFilePaths().size(), 8);
  //
  //    QueryExpression nextPageQueryExpression = firstPage.getNextPageQueryExpression().get();
  //    nextPageQueryExpression.setMaxItems(100);
  //
  //    SearchResult lastPage = searcher.search(nextPageQueryExpression);
  //    assertEquals(lastPage.getFilePaths().size(), 17);
  //
  //    assertTrue(Collections.disjoint(firstPage.getFilePaths(), lastPage.getFilePaths()));
  //  }

  public void assertFind(QueryExpression query, String... expectedPaths) throws ServerException {
    List<String> paths = searcher.search(query).getFilePaths();
    assertEquals(paths, Arrays.asList(expectedPaths));
  }

  public void assertEmptyResult(QueryExpression query) throws ServerException {
    List<String> paths = searcher.search(query).getFilePaths();
    assertTrue(paths.isEmpty());
  }

  public static class ContentBuilder {
    private Path root;
    private Path lastUpdatedFile;

    public ContentBuilder(Path root) {
      this.root = root;
    }

    public ContentBuilder createFolder(String name) throws IOException {
      this.root = Paths.get(this.root.toString(), name);
      java.nio.file.Files.createDirectories(this.root);
      return this;
    }

    public ContentBuilder createFile(String name, String content) throws IOException {
      this.lastUpdatedFile = Paths.get(this.root.toString(), name);
      Files.write(content.getBytes(), lastUpdatedFile.toFile());
      return this;
    }

    public Path getCurrentFolder() {
      return this.root;
    }

    public Path getLastUpdatedFile() {
      return lastUpdatedFile;
    }
  }
}
