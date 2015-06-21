package at.ac.tuwien.big.momot.examples.university.average;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.interpreter.util.InterpreterUtil;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;

import at.ac.tuwien.big.moea.run.executor.EvolutionarySearchExecutorFactory;
import at.ac.tuwien.big.moea.run.experiment.Experiment;
import at.ac.tuwien.big.moea.run.listener.SeedRuntimePrintListener;
import at.ac.tuwien.big.moea.util.CastUtil;
import at.ac.tuwien.big.momot.MomotFactory;
import at.ac.tuwien.big.momot.MomotOrchestration;
import at.ac.tuwien.big.momot.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.fitness.dimension.MatchSolutionLengthDimension;
import at.ac.tuwien.big.momot.match.ExecutionResult;
import at.ac.tuwien.big.momot.operator.mutation.MatchParameterMutation;
import at.ac.tuwien.big.momot.operator.mutation.MatchPlaceholderMutation;
import at.ac.tuwien.big.momot.rule.parameter.fix.FixValue;
import at.ac.tuwien.big.momot.solution.MatchSolution;
import at.ac.tuwien.big.momot.solution.repair.MatchPlaceholderSolutionRepairer;

public class AverageOrchestration {
	private static final String MODEL = "university_professors.xmi";
	private static final String PATH = "model/" + MODEL;

	private static final String MODEL_EXTENSION = ".xmi";

	private static final String RULES = "university_professor.henshin";
	private static final String MODEL_DIRECTORY = "model/";
	private static final String OUTPUT_DIRECTORY = "output/";

	private static final String RULE_CREATECERTIFICATE_CERTNOTE = "createCertificate.certnote";
	private static final String RULE_CREATECERTIFICATE_MNR = "createCertificate.mnr";
	private static final String RULE_CREATECERTIFICATE_COURSENAME = "createCertificate.coursename";

	private static final String RULE_CALCULATEAVERAGEGRADE_NOTE = "calculateAverageGrade.note";
	private static final String RULE_CALCULATEAVERAGEGRADE_GRADEAVERAGE = "calculateAverageGrade.gradeaverage";

	private static final String RULE_COUNTATTENDANTS_CERT = "countAttendants.cert";
	private static final String RULE_COUNTATTENDANTS_NROLD = "countAttendants.nrold";
	private static final String RULE_COUNTATTENDANTS_COURSENAMEA = "countAttendants.coursenamea";

	private static final String RULE_COUNTSTUDENTS_S = "countStudents.s";
	private static final String RULE_COUNTSTUDENTS_N = "countStudents.n";
	
	private static final String RULE_CALCULATEAVERAGEDURATION_N = "calculateAverageDuration.n";
	private static final String RULE_CALCULATEAVERAGEDURATION_AVERAGELENGTH = "calculateAverageDuration.averageLength";
	
	private static final int SEARCH_POPULATION = 100;
	private static final int SEARCH_ITERATIONS = 200;
	private static final int SEARCH_SOLUTIONLENGTH = 10;

	public static MomotOrchestration createOrchestration(String initialModel) {
		Engine engine = new EngineImpl();
		MomotFactory factory = new MomotFactory(engine, MODEL_DIRECTORY, RULES);
		EGraph graph = factory.loadGraph(initialModel);		
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);
		
		MomotOrchestration orchestration = new MomotOrchestration(factory,
				graph, SEARCH_SOLUTIONLENGTH);

		// use the fitness function
		orchestration.getFitnessFunction().saveExecutionResult(true);
		orchestration.getFitnessFunction().addObjective(
				new AbstractEGraphFitnessDimension("NrViolations") {
					@Override
					protected double evaluate(MatchSolution solution,
							ExecutionResult executionResult) {
						EGraph result = executionResult.getResultGraph();
						EObject root = result.getRoots().get(0);
						Diagnostic validation = Diagnostician.INSTANCE
								.validate(root);
						return validation.getChildren().size();
					}
				});
		orchestration.getFitnessFunction().addObjective(
				new MatchSolutionLengthDimension());
		orchestration.getFitnessFunction().setSolutionRepairer(
				new MatchPlaceholderSolutionRepairer());

		// specify parameters of interest (preserved in the solution sequence,
		// see output)
/*		orchestration.getParameterManager().preserveParameters(
				RULE_CREATECERTIFICATE_CERTNOTE, RULE_CREATECERTIFICATE_MNR,
				RULE_CREATECERTIFICATE_COURSENAME,
				RULE_CALCULATEAVERAGEGRADE_NOTE,
				RULE_CALCULATEAVERAGEGRADE_GRADEAVERAGE,
				RULE_COUNTATTENDANTS_CERT, RULE_COUNTATTENDANTS_NROLD,
				RULE_COUNTATTENDANTS_COURSENAMEA);*/
		orchestration.getParameterManager().preserveParameters(
				RULE_CREATECERTIFICATE_CERTNOTE, RULE_CREATECERTIFICATE_MNR,
				RULE_CREATECERTIFICATE_COURSENAME);
		orchestration.getParameterManager().preserveParameters(
				RULE_COUNTSTUDENTS_S,
				RULE_COUNTSTUDENTS_N);
		orchestration.getParameterManager().preserveParameters(
				RULE_CALCULATEAVERAGEDURATION_N,
				RULE_CALCULATEAVERAGEDURATION_AVERAGELENGTH);

		// user parameters, i.e., parameters that can not be matched based on
		// the existing model
/*		orchestration.getParameterManager().setParameterValue(
				RULE_CREATECERTIFICATE_CERTNOTE, new FixValue<Integer>(5));
		orchestration.getParameterManager().setParameterValue(
				RULE_CREATECERTIFICATE_MNR, new FixValue<String>("S2"));
		orchestration.getParameterManager().setParameterValue(
				RULE_CREATECERTIFICATE_COURSENAME, new FixValue<String>("C2"));*/

		/*
		 * orchestration.getParameterManager().setParameterValue(
		 * RULE_CALCULATEAVERAGEGRADE_NOTE, new FixValue<Integer>(5));
		 * orchestration.getParameterManager().setParameterValue(
		 * RULE_CALCULATEAVERAGEGRADE_GRADEAVERAGE, new FixValue<Double>(0.0));
		 */

		/*
		 * orchestration.getParameterManager().setParameterValue(
		 * RULE_COUNTATTENDANTS_NROLD, new FixValue<Integer>(0));
		 * orchestration.getParameterManager().setParameterValue(
		 * RULE_COUNTATTENDANTS_COURSENAMEA, new FixValue<String>("C1"));
		 */

		orchestration.getSolutionPrinter().setPrintPlaceholders(false);
		return orchestration;
	}

	/*public static void run(String path) {

		// Create a resource set with a base directory:
		HenshinResourceSet resourceSet = new HenshinResourceSet("model");

		// Load the module:
		Module module = resourceSet.getModule(RULES, false);

		// Load the example model into an EGraph:
		EGraph graph = new EGraphImpl(resourceSet.getResource(MODEL));

		// Get the family object:
		EObject university = graph.getRoots().get(0);
		List<EObject> courses = (List<EObject>) university.eGet(university
				.eClass().getEStructuralFeature("courses"));

		// Create an engine and a rule application:
		Engine engine = new EngineImpl();
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);

		// Calculation number of family members:
		app.setUnit(module.getUnit("countStudents"));
		InterpreterUtil.executeOrDie(app);
		// Calculation number of family members:

		// Calculation number of family members:
		app.setUnit(module.getUnit("calculateAverageDuration"));
		InterpreterUtil.executeOrDie(app);
		// Calculation number of family members:
		
		app.setUnit(module.getUnit("countAttendants"));
		InterpreterUtil.executeOrDie(app);

		Double averageLength = (Double) university.eGet(university.eClass()
				.getEStructuralFeature("averageLength"));
		System.out.println("average duration: " + averageLength);
		
		Integer studentNumber = (Integer) university.eGet(university.eClass()
				.getEStructuralFeature("numberOfStudents"));
		System.out.println("student number: " + studentNumber);

		for (EObject c : courses) {
			String name = (String) c.eGet(c.eClass().getEStructuralFeature(
					"name"));
			int memberCount = (Integer) c.eGet(c.eClass()
					.getEStructuralFeature("numberOfAttendants"));
			double gradeAverage = (Double) c.eGet(c.eClass()
					.getEStructuralFeature("gradeAverage"));
			System.out.println("Course name: " + name);
			System.out.println("Number of attendants: " + memberCount);
			System.out.println("Grade average: " + gradeAverage);
		}

		
		 * if (memberCount != 4) { throw new
		 * RuntimeException("Expected attendants"); }
		 

		
		 * // Calculation average age:
		 * app.setUnit(module.getUnit("calcAverageAge"));
		 * InterpreterUtil.executeOrDie(app);
		 * 
		 * // We expect an average age of 20: double averageAge = (Double)
		 * university
		 * .eGet(university.eClass().getEStructuralFeature("averageAge"));
		 * System.out.println("Calculated " + averageAge + " as average age");
		 * if (averageAge != 20) { throw new
		 * RuntimeException("Expected average age of 20"); }
		 

	}*/

	public static void addEvolutionarySearch(MomotOrchestration orchestration,
			Experiment experiment) {
		EvolutionarySearchExecutorFactory<MatchSolution> evolutionaryExecutor = orchestration
				.createEvolutionaryExecutorFactory(SEARCH_POPULATION,
						SEARCH_ITERATIONS, new TournamentSelection(4),
						new OnePointCrossover(1), new MatchPlaceholderMutation(
								0.1), new MatchParameterMutation(0.1,
								orchestration.getParameterManager()));

		experiment.addExecutor("Random",
				evolutionaryExecutor.createRandomSearchExecutor());
		experiment.addExecutor("NSGA-II",
				evolutionaryExecutor.createNSGAIIExecutor());
		experiment.addExecutor("NSGA-III",
				evolutionaryExecutor.createNSGAIIIExecutor(0, 4));
		experiment.addExecutor("EpsilonMOEA",
				evolutionaryExecutor.createEpsilonMOEAExecutor(0.05));
	}

	public static void saveSolutions(MomotOrchestration orchestration,
			NondominatedPopulation solutions, String inputModel) {
		String outputPrefix = getOutputModelPrefix(inputModel);

		for (Solution solution : solutions) {
			MatchSolution matchSolution = CastUtil.asClass(solution,
					MatchSolution.class);

			if (matchSolution == null)
				continue;

			// ensure unique names of output files
			String outputFile = outputPrefix
					+ "_"
					+ new Double(matchSolution.getObjective(orchestration
							.getFitnessFunction().getObjectiveIndex(
									"NrViolations"))).intValue()
					+ MODEL_EXTENSION;
			try {
				orchestration.save(
						matchSolution.getStoredResultGraph().copy(null),
						outputFile);
				System.out.println(outputFile + ": ok");
			} catch (Exception e) {
				System.err.println(outputFile + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static String getOutputModelPrefix(String inputModel) {
		String subDirectory = OUTPUT_DIRECTORY;
		String filePrefix = inputModel.substring(0, inputModel.length()
				- MODEL_EXTENSION.length());
		try {
			subDirectory += filePrefix + "/";
			FileUtils.forceMkdir(new File(MODEL_DIRECTORY + subDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return subDirectory + filePrefix;
	}

	public static String getReferenceFile(String inputModel) {
		String subDirectory = MODEL_DIRECTORY + OUTPUT_DIRECTORY;
		String filePrefix = inputModel.substring(0, inputModel.length()
				- MODEL_EXTENSION.length());
		try {
			subDirectory += filePrefix + "/";
			FileUtils.forceMkdir(new File(subDirectory));
			FileUtils.cleanDirectory(new File(subDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return subDirectory + filePrefix + "_reference.csv";
	}

	public static void printReferenceSet(MomotOrchestration orchestration,
			String referenceFile) {
		System.out.println("------------------------------------");
		System.out.println("ReferenceSet"
				+ orchestration.getFitnessFunction().getObjectiveNames() + "");
		System.out.println("------------------------------------");
		try {
			System.out.println(new String(Files.readAllBytes(Paths
					.get(referenceFile))));
		} catch (IOException e) {
			System.out.println("Error reading reference file: "
					+ e.getMessage());
		}
	}

	public static void repairModel(String inputModel, int algorithmRuns) {
		MomotOrchestration orchestration = createOrchestration(inputModel);

		String referenceFile = getReferenceFile(inputModel);

		Experiment experiment = new Experiment()
				.useProgressListener(new SeedRuntimePrintListener())
				.saveReferenceFile(getReferenceFile(inputModel))
				.printLogging(true);

		addEvolutionarySearch(orchestration, experiment);

		experiment.run(algorithmRuns); // run each provided algorithm n times

		NondominatedPopulation solutions = experiment.getReferenceSet();
		System.out.println(orchestration.print(solutions));
		printReferenceSet(orchestration, referenceFile);

		saveSolutions(orchestration, solutions, inputModel);
	}

	public static void main(String[] args) throws IOException {
		// repairModel(MODEL, 1);

		//run(PATH);
		repairModel(MODEL, 1);
	}
}
