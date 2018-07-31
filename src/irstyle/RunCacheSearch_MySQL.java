package irstyle;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import irstyle_core.JDBCaccess;
import irstyle_core.Relation;
import irstyle_core.Schema;
import query.ExperimentQuery;
import query.QueryServices;
import wiki13.WikiFilesPaths;

public class RunCacheSearch_MySQL {

	public static void main(String[] args) throws Exception {

		JDBCaccess jdbcacc = IRStyleKeywordSearch.jdbcAccess();
		WikiFilesPaths paths = null;
		paths = WikiFilesPaths.getMaplePaths();
		List<ExperimentQuery> queries = QueryServices.loadMsnQueries(paths.getMsnQueryFilePath(),
				paths.getMsnQrelFilePath());
		Collections.shuffle(queries, new Random(1));
		queries = queries.subList(0, 50);
		List<IRStyleQueryResult> queryResults = new ArrayList<IRStyleQueryResult>();
		String baseDir = "/data/ghadakcv/wikipedia/";
		try (IndexReader articleReader = DirectoryReader
				.open(FSDirectory.open(Paths.get(baseDir + "tbl_article_09/100")));
				IndexReader articleCacheReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_article_09/3")));
				IndexReader articleRestReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_article_09/c3")));
				IndexReader imageReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_image_pop/100")));
				IndexReader imageCacheReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_image_pop/10")));
				IndexReader imageRestReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_image_pop/c10")));
				IndexReader linkReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_link_pop/100")));
				IndexReader linkCacheReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_link_pop/6")));
				IndexReader linkRestReader = DirectoryReader
						.open(FSDirectory.open(Paths.get(baseDir + "tbl_link_pop/c6")))) {
			int loop = 1;
			for (ExperimentQuery query : queries) {
				System.out.println("processing query " + loop++ + "/" + queries.size() + ": " + query.getText());
				Vector<String> allkeyw = new Vector<String>();
				// escaping single quotes
				allkeyw.addAll(Arrays.asList(query.getText().replace("'", "\\'").split(" ")));
				String articleTable = "tbl_article_09";
				String imageTable = "tbl_image_09_tk";
				String linkTable = "tbl_link_09";
				String articleImageTable = "tbl_article_image_09";
				String articleLinkTable = "tbl_article_link_09";
				long time1 = System.currentTimeMillis();
				if (CacheLanguageModel.useCache(query.getText(), articleCacheReader, articleReader,
						articleRestReader)) {
					articleTable = "sub_article_3";
				}
				if (CacheLanguageModel.useCache(query.getText(), imageCacheReader, imageReader, imageRestReader)) {
					imageTable = "sub_image_10";
				}
				if (CacheLanguageModel.useCache(query.getText(), linkCacheReader, linkReader, linkRestReader)) {
					linkTable = "sub_link_6";
				}
				long time2 = System.currentTimeMillis();
				System.out.println(" Time to select cache: " + (time2 - time1) + " (ms)");
				String schemaDescription = "5 " + articleTable + " " + articleImageTable + " " + imageTable + " "
						+ articleLinkTable + " " + linkTable + " " + articleTable + " " + articleImageTable + " "
						+ articleImageTable + " " + imageTable + " " + articleTable + " " + articleLinkTable + " "
						+ articleLinkTable + " " + linkTable;
				Schema sch = new Schema(schemaDescription);
				Vector<Relation> relations = IRStyleKeywordSearch.createRelations(articleTable, imageTable, linkTable,
						jdbcacc.conn);

				List<String> articleIds = RunBaseline_Lucene.executeLuceneQuery(articleReader, query.getText());
				List<String> imageIds = RunBaseline_Lucene.executeLuceneQuery(imageReader, query.getText());
				List<String> linkIds = RunBaseline_Lucene.executeLuceneQuery(linkReader, query.getText());
				Map<String, List<String>> relnamesValues = new HashMap<String, List<String>>();
				relnamesValues.put(articleTable, articleIds);
				relnamesValues.put(imageTable, imageIds);
				relnamesValues.put(linkTable, linkIds);
				IRStyleQueryResult result = RunBaseline_Lucene.executeIRStyleQuery(jdbcacc, sch, relations, query,
						relnamesValues);
				queryResults.add(result);
			}
			IRStyleKeywordSearch.printRrankResults(queryResults, "cs_result.csv");

		}
	}

}