package wiki13;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.search.similarities.BM25Similarity;

import indexing.InexFile;
import popularity.PopularityUtils;
import query.ExperimentQuery;
import query.QueryResult;
import query.QueryServices;

public class WikiMapleExperiment {

	private static final Logger LOGGER = Logger.getLogger(WikiMapleExperiment.class.getName());
	private static final String DATA_PATH = "/data/ghadakcv/data/";
	private static final String INDEX_PATH = DATA_PATH + "wiki_index";
	private static final String FILELIST_PATH = DATA_PATH + "/wiki13_count09_text.csv";
	private static final String RESULT_FILE_PATH = DATA_PATH + "/wiki_result.csv";

	public static void main(String[] args) {
		if (args.length < 1) {
			LOGGER.log(Level.SEVERE, "A flag should be specified. Available flags are: \n\t--index\n\t--query\n");
		} else if (args[0].equals("--index")) {
			buildIndex(FILELIST_PATH, INDEX_PATH);
		} else if (args[0].equals("--query")) {
			List<QueryResult> results = runQueriesOnGlobalIndex(INDEX_PATH, "", "");
			Map<String, Double> idPopMap = PopularityUtils.loadIdPopularityMap(FILELIST_PATH);
			results = filterResults(results, idPopMap);
			writeResultsToFile(results, RESULT_FILE_PATH);
		}
	}

	private static void buildIndex(String fileListPath, String indexDirectoryPath) {
		try {
			List<InexFile> pathCountList = InexFile.loadInexFileList(fileListPath);
			LOGGER.log(Level.INFO, "Number of loaded path_counts: " + pathCountList.size());
			File indexPathFile = new File(indexDirectoryPath);
			if (!indexPathFile.exists()) {
				indexPathFile.mkdirs();
			}
			LOGGER.log(Level.INFO, "Building index at: " + indexDirectoryPath);
			Wiki13Indexer.buildIndexOnText(pathCountList, indexDirectoryPath, new BM25Similarity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<QueryResult> runQueriesOnGlobalIndex(String indexPath, String queriesFilePath,
			String qrelsFilePath) {
		LOGGER.log(Level.INFO, "Loading queries..");
		List<ExperimentQuery> queries = QueryServices.loadInexQueries(queriesFilePath, qrelsFilePath);
		LOGGER.log(Level.INFO, "Number of loaded queries: " + queries.size());
		Map<String, Float> fieldToBoost = new HashMap<String, Float>();
		fieldToBoost.put(Wiki13Indexer.TITLE_ATTRIB, 0.1f);
		fieldToBoost.put(Wiki13Indexer.CONTENT_ATTRIB, 0.9f);
		LOGGER.log(Level.INFO, "Running queries..");
		List<QueryResult> results = QueryServices.runQueriesWithBoosting(queries, indexPath, new BM25Similarity(),
				fieldToBoost);
		return results;
	}

	static List<QueryResult> filterResults(List<QueryResult> results, Map<String, Double> idPopMap) {
		LOGGER.log(Level.INFO, "Caching results..");
		for (QueryResult result : results) {
			filterQueryResult(result, idPopMap, 1);
		}
		return results;
	}

	static void filterQueryResult(QueryResult result, Map<String, Double> idPopMap, double cutoffSize) {
		if (result.getTopResults().size() < 2) {
			LOGGER.log(Level.WARNING, "query just has zero or one result");
			return;
		}
		List<Double> pops = new ArrayList<Double>();
		for (String id : result.getTopResults()) {
			pops.add(idPopMap.get(id));
		}
		Collections.sort(pops, Collections.reverseOrder());
		double cutoffWeight = pops.get((int)Math.floor(cutoffSize * pops.size()) - 1);
		System.out.println(cutoffWeight);
		List<String> newTopResults = new ArrayList<String>();
		List<String> newTopResultTitles = new ArrayList<String>();
		for (int i = 0; i < result.getTopResults().size(); i++) {
			if (idPopMap.get(result.getTopResults().get(i)) >= cutoffWeight) {
				newTopResults.add(result.getTopResults().get(i));
				newTopResultTitles.add(result.getTopResultsTitle().get(i));
			}
		}
		result.setTopResults(newTopResults);
		result.setTopResultsTitle(newTopResultTitles);
	}
	
	public static void writeResultsToFile(List<QueryResult> results, String resultFilePath) {
		LOGGER.log(Level.INFO, "Writing results..");
		try (FileWriter fw = new FileWriter(resultFilePath)) {
			for (QueryResult iqr : results) {
				fw.write(iqr.resultString() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
