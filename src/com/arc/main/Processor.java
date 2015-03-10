package com.arc.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arc.model.FileType;
import com.arc.model.Resource;
import com.arc.model.ResourceType;
import com.arc.utils.FileUtils;

public class Processor {
	private volatile boolean isCanceled = false;
	private String mPackageName;
	private ARCLoader mCallback;

	private File mBaseDirectory;
	private List<File> mSrcDirectories = new ArrayList<File>();
	private File mResDirectory;
	private File mGenDirectory;
	private File mManifestFile;
	private File mRJavaFile;

	private List<Resource> mResList = new ArrayList<Resource>();
	private final Set<Resource> mResources = new HashSet<Resource>();
	private final Set<Resource> mUsedResources = new HashSet<Resource>();

	private static final Pattern sResourceTypePattern = Pattern
			.compile("^\\s*public static final class (\\w+)\\s*\\{$");
	private static final Pattern sResourceNamePattern = Pattern
			.compile("^\\s*public static( final)? int(\\[\\])? (\\w+)\\s*=\\s*(\\{|(0x)?[0-9A-Fa-f]+;)\\s*$");
	private static final FileType sJavaFileType = new FileType("java", "R."
			+ FileType.USAGE_TYPE + "." + FileType.USAGE_NAME + "[^\\w_]");
	private static final FileType sXmlFileType = new FileType("xml", "[\" >]@"
			+ FileType.USAGE_TYPE + "/" + FileType.USAGE_NAME + "[\" <]");

	private static Map<String, ResourceType> sResourceTypes = ResourceTypes.sResourceTypes;

	private SimpleDateFormat mDateFormat = new SimpleDateFormat(
			"yyyy年MM月dd日   HH:mm:ss");

	

	public Processor() {
		super();
		final String baseDirectory = System.getProperty("user.dir");
		mBaseDirectory = new File(baseDirectory);
	}

	protected Processor(final String baseDirectory) {
		super();
		mBaseDirectory = new File(baseDirectory);
	}

	public void delete(int[] list) {
		LinkedList<Resource> remove = new LinkedList<Resource>();
		for (int i : list) {
			Resource result = mResList.get(i);
			final String type = result.getType();
			System.out.println("type: " + type);
			if (type.equals("anim") || type.equals("drawable")
					|| type.equals("layout")) {
				final String path = result.getPath();
				System.out.println("path: " + path);
				if (!isEmpty(path)) {
					final Pattern pattern = Pattern.compile(".png|.jpg"); //匹配图片
					final Matcher matcher = pattern.matcher(path);
					if (matcher.find()) {
						File file = new File(path);
						file.delete();
						remove.add(result);
					}
				}
			}
		}
		System.out.println("remove " + remove.size() + " images");
		mResList.removeAll(remove);
	}

	public String[] getResult() {
		String[] result = new String[mResList.size()];
		for (int i = 0; i < mResList.size(); i++) {
			result[i] = mResList.get(i).toString();
		}
		return result;
	}

	public void deleteAll() {
		LinkedList<Resource> remove = new LinkedList<Resource>();
		for (Resource resource : mResList) {
			final String type = resource.getType();
			if (type.equals("anim") || type.equals("drawable")
					|| type.equals("layout") || type.equals("menu")) {
				final String path = resource.getPath();
				if (!isEmpty(path)) {
					File file = new File(path);
					file.delete();
					remove.add(resource);
				}
			}
		}
		mResList.removeAll(remove);
	}

	private boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}

	public void showResult() {
		if (mCallback != null) {
			mCallback.setResult(mResList);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (mResList != null) {
			mResList.clear();
		}
		super.finalize();
	}

	public void run(ARCLoader callback) {

		isCanceled = false;

		if (callback != null) {
			this.mCallback = callback;
		}

		System.out.println("Running in: " + mBaseDirectory.getAbsolutePath());

		findPaths(mBaseDirectory);
		System.out.println("mResDirectory:" + mResDirectory.getAbsolutePath());
		System.out.println("mManifestFile:" + mManifestFile.getAbsolutePath());
		for (File sFile : mSrcDirectories) {
			System.out.println("mSrcDirectory:" + sFile.getAbsolutePath());
		}

		if (mSrcDirectories.isEmpty() || mResDirectory == null
				|| mManifestFile == null) {
			System.err
					.println("The current directory is not a valid Android project root.");
			return;
		}

		mPackageName = findPackageName(mManifestFile);

		if (mPackageName == null || mPackageName.trim().length() == 0) {
			return;
		}

		if (mGenDirectory == null) {
			System.err
					.println("You must first build your project to generate R.java");
			return;
		}

		mRJavaFile = findRJavaFile(mGenDirectory, mPackageName);

		if (mRJavaFile == null) {
			System.err
					.println("You must first build your project to generate R.java");
			return;
		}

		mResources.clear();

		try {
			mResources.addAll(getResourceList(mRJavaFile));// 查找R文件
		} catch (final IOException e) {
			System.err.println("The R.java found could not be opened.");
			e.printStackTrace();
		}

		if (isCanceled) {
			return;
		}

		System.out.println(mResources.size() + " resources found");
		System.out.println();

		mUsedResources.clear();
		long start_time = System.currentTimeMillis();
		System.out.println("Start search >> "
				+ mDateFormat.format(new Date(start_time)));
		for (File sFile : mSrcDirectories) {
			searchFiles(null, sFile, sJavaFileType);
		}
		if (isCanceled) {
			return;
		}

		searchFiles(null, mResDirectory, sXmlFileType);
		if (isCanceled) {
			return;
		}

		searchFiles(null, mManifestFile, sXmlFileType);
		if (isCanceled) {
			return;
		}

		/*
		 * Because attr and styleable are so closely linked, we need to do some
		 * matching now to ensure we don't say an attr is unused if its
		 * corresponding styleable is used.
		 */
		final Set<Resource> extraUsedResources = new HashSet<Resource>();

		for (final Resource resource : mResources) {
			if (resource.getType().equals("styleable")) {
				final String[] styleableAttr = resource.getName().split("_");

				if (styleableAttr.length > 1) {
					final String attrName = styleableAttr[1];

					final Resource attrResourceTest = new Resource("attr", attrName);

					if (mUsedResources.contains(attrResourceTest)) {
						extraUsedResources.add(resource);
					}
				}
			} else if (resource.getType().equals("attr")) {
				for (final Resource usedResource : mUsedResources) {
					if (usedResource.getType().equals("styleable")) {
						final String[] styleableAttr = usedResource.getName()
								.split("_");

						if (styleableAttr.length > 1
								&& styleableAttr[1].equals(resource.getName())) {
							extraUsedResources.add(resource);
						}
					}
				}
			}
		}

		for (final Resource resource : extraUsedResources) {
			mResources.remove(resource);
			mUsedResources.add(resource);
		}

		final SortedMap<String, SortedMap<String, Resource>> unusedResources = new TreeMap<String, SortedMap<String, Resource>>();

		for (final Resource resource : mResources) {
			final String type = resource.getType();
			SortedMap<String, Resource> typeMap = unusedResources.get(type);

			if (typeMap == null) {
				typeMap = new TreeMap<String, Resource>();
				unusedResources.put(type, typeMap);
			}

			typeMap.put(resource.getName(), resource);
		}

		final Map<String, ResourceType> unusedResourceTypes = new HashMap<String, ResourceType>(
				unusedResources.size());

		for (final String type : unusedResources.keySet()) {
			final ResourceType resourceType = sResourceTypes.get(type);
			if (resourceType != null) {
				unusedResourceTypes.put(type, resourceType);
			}
		}

		findDeclaredPaths(null, mResDirectory, unusedResourceTypes,
				unusedResources);

		/*
		 * Find the paths where the used resources are declared.
		 */
		final SortedMap<String, SortedMap<String, Resource>> usedResources = new TreeMap<String, SortedMap<String, Resource>>();

		for (final Resource resource : mUsedResources) {
			final String type = resource.getType();
			SortedMap<String, Resource> typeMap = usedResources.get(type);

			if (typeMap == null) {
				typeMap = new TreeMap<String, Resource>();
				usedResources.put(type, typeMap);
			}

			typeMap.put(resource.getName(), resource);
		}

		if (isCanceled) {
			return;
		}

		final Map<String, ResourceType> usedResourceTypes = new HashMap<String, ResourceType>(
				usedResources.size());

		for (final String type : usedResources.keySet()) {
			final ResourceType resourceType = sResourceTypes.get(type);
			if (resourceType != null) {
				usedResourceTypes.put(type, resourceType);
			}
		}

		if (isCanceled) {
			return;
		}

		findDeclaredPaths(null, mResDirectory, usedResourceTypes, usedResources);

		final Set<Resource> libraryProjectResources = getLibraryProjectResources();

		for (final Resource libraryResource : libraryProjectResources) {
			final SortedMap<String, Resource> typedResources = unusedResources
					.get(libraryResource.getType());

			if (typedResources != null) {
				final Resource appResource = typedResources.get(libraryResource
						.getName());

				if (appResource != null && appResource.hasNoDeclaredPaths()) {
					typedResources.remove(libraryResource.getName());
					mUsedResources.add(appResource);
					mResources.remove(appResource);
				}
			}
		}

		if (isCanceled) {
			return;
		}
		long end_time = System.currentTimeMillis();
		System.out.println("End search >>: "
				+ mDateFormat.format(new Date(end_time)));
		System.out.println("duration: " + (end_time - start_time) / 1000 + "s");

		final int unusedResourceCount = mResources.size();

		if (unusedResourceCount > 0) {
			System.out.println(unusedResourceCount
					+ " unused resources were found.");

			final SortedSet<Resource> sortedResources = new TreeSet<Resource>(
					mResources);

			try {
				FileWriter writer = new FileWriter(
						mBaseDirectory.getAbsolutePath() + "\\" + "unused.txt");

				for (final Resource resource : sortedResources) {
					writer.write(resource.toString());
					writer.write("\r\n");
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mResList.addAll(sortedResources);
		}

		// record used resource
		if (mUsedResources.size() > 0) {
			System.out.println(mUsedResources.size()
					+ " used resources were found.");

			final SortedSet<Resource> sortedResources = new TreeSet<Resource>(
					mUsedResources);

			try {
				FileWriter writer = new FileWriter(
						mBaseDirectory.getAbsolutePath() + "\\" + "used.txt");

				for (final Resource resource : sortedResources) {
					writer.write(resource.getType() + ": " +resource.getName()+" -> " + resource.getReferencePath());
					writer.write("\r\n");
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		showResult();
	}

	private void findPaths(File baseDirecotry) {
		final File[] children = baseDirecotry.listFiles();

		if (children == null) {
			return;
		}

		// 判断项目类型，多项目或单项目
		String project_type = "muli";
		for (final File file : children) {
			if (!file.isDirectory() && file.getName().equals(".project")) {
				project_type = "single";
				break;
			}
		}

		// single project
		if (project_type.equals("single")) {
			for (final File file : children) {
				if (file.isDirectory()) {
					if (file.getName().equals("src")) {
						mSrcDirectories.add(file);
					} else if (file.getName().equals("res")) {
						mResDirectory = file;
					} else if (file.getName().equals("gen")) {
						mGenDirectory = file;
					}
				} else if (file.getName().equals("AndroidManifest.xml")) {
					mManifestFile = file;
				}
			}
		} else {// muti project
			for (final File file : children) {
				if (!file.isDirectory() || !file.getName().contains("weibo")) {
					continue;
				}
				if (file.getName().contains("res")) {
					for (final File resFile : file.listFiles()) {
						if (resFile.isDirectory()) {
							if (resFile.getName().equals("res")) {
								mResDirectory = resFile;
							} else if (resFile.getName().equals("gen")) {
								mGenDirectory = resFile;
							}
						} else if (resFile.getName().equals(
								"AndroidManifest.xml")) {
							mManifestFile = resFile;
						}
					}
				} else {
					for (final File srcFile : file.listFiles()) {
						if (srcFile.isDirectory()) {
							if (srcFile.getName().equals("src")) {
								mSrcDirectories.add(srcFile);
							}
						}
					}
				}
			}
		}
	}

	private static String findPackageName(final File androidManifestFile) {
		String manifest = "";

		try {
			manifest = FileUtils.getFileContents(androidManifestFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final Pattern pattern = Pattern
				.compile("<manifest\\s+.*?package\\s*=\\s*\"([A-Za-z0-9_\\.]+)\".*?>");
		final Matcher matcher = pattern.matcher(manifest);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	private static File findRJavaFile(final File baseDirectory,
			final String packageName) {
		final File rJava = new File(baseDirectory,
				packageName.replace('.', '/') + "/R.java");

		if (rJava.exists()) {
			return rJava;
		}

		return null;
	}

	public void setbCancel(boolean bCancel) {
		this.isCanceled = bCancel;
	}

	/**
	 * Removes all resources declared in library projects.
	 */
	private Set<Resource> getLibraryProjectResources() {
		final Set<Resource> resources = new HashSet<Resource>();

		final File projectPropertiesFile = new File(mBaseDirectory,
				"project.properties");

		if (!projectPropertiesFile.exists()) {
			return resources;
		}

		List<String> fileLines = new ArrayList<String>();
		try {
			fileLines = FileUtils.getFileLines(projectPropertiesFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final Pattern libraryProjectPattern = Pattern.compile(
				"^android\\.library\\.reference\\.\\d+=(.*)$",
				Pattern.CASE_INSENSITIVE);

		final List<String> libraryProjectPaths = new ArrayList<String>();

		for (final String line : fileLines) {
			final Matcher libraryProjectMatcher = libraryProjectPattern
					.matcher(line);

			if (libraryProjectMatcher.find()) {
				libraryProjectPaths.add(libraryProjectMatcher.group(1));
			}
		}

		for (final String libraryProjectPath : libraryProjectPaths) {
			final File libraryProjectDirectory = new File(mBaseDirectory,
					libraryProjectPath);

			if (libraryProjectDirectory.exists()
					&& libraryProjectDirectory.isDirectory()) {
				final String libraryProjectPackageName = findPackageName(new File(
						libraryProjectDirectory, "AndroidManifest.xml"));
				final File libraryProjectRJavaFile = findRJavaFile(new File(
						libraryProjectDirectory, "gen"),
						libraryProjectPackageName);

				if (libraryProjectRJavaFile != null) {
					try {
						resources
								.addAll(getResourceList(libraryProjectRJavaFile));
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return resources;
	}

	private static Set<Resource> getResourceList(final File rJavaFile)
			throws IOException {
		final InputStream inputStream = new FileInputStream(rJavaFile);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		boolean done = false;

		final Set<Resource> resources = new HashSet<Resource>();

		String type = "";
		boolean isType = false;

		while (!done) {
			final String line = reader.readLine();
			done = (line == null);

			if (line != null) {
				final Matcher typeMatcher = sResourceTypePattern.matcher(line);
				final Matcher nameMatcher = sResourceNamePattern.matcher(line);

				if (typeMatcher.find()) {
					type = typeMatcher.group(1);
					if (type.equals("drawable")) {
						isType = true;
					} else {
						isType = false;
					}
				}
				if (isType && nameMatcher.find()) {
					resources.add(new Resource(type, nameMatcher.group(3)));
				}
				/*
				 * if (nameMatcher.find()) { resources.add(new ResSet(type,
				 * nameMatcher.group(3))); } else if (typeMatcher.find()) { type
				 * = typeMatcher.group(1); }
				 */
			}
		}

		reader.close();
		inputStream.close();

		return resources;
	}

	private void searchFiles(final File parent, final File file,
			final FileType fileType) {
		if (isCanceled) {
			return;
		}
		if (file.isDirectory()) {
			for (final File child : file.listFiles()) {
				if (isCanceled) {
					return;
				}
				searchFiles(file, child, fileType);
			}
		} else if (file.getName().endsWith(fileType.getExtension())) {
			try {
				if (isCanceled) {
					return;
				}
				searchFile(parent, file, fileType);
				if (mCallback != null) {
					mCallback.setProgress(file);
				}
			} catch (final IOException e) {
				System.err.println("There was a problem reading "
						+ file.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}

	private void searchFile(final File parent, final File file,
			final FileType fileType) throws IOException {
		final Set<Resource> foundResources = new HashSet<Resource>();

		final String fileContents = FileUtils.getFileContents(file);

		for (final Resource resource : mResources) {
			if (isCanceled) {
				return;
			}
			final Matcher matcher = fileType.getPattern(resource.getType(),
					resource.getName().replace("_", "[_\\.]")).matcher(
					fileContents);

			if (matcher.find()) {
				foundResources.add(resource);
				resource.setReferencePath(file.getAbsolutePath());
			} else {
				final ResourceType type = sResourceTypes.get(resource.getType());

				if (type != null
						&& type.doesFileUseResource(parent, file.getName(),
								fileContents,
								resource.getName().replace("_", "[_\\.]"))) {
					foundResources.add(resource);
					resource.setReferencePath(file.getAbsolutePath());
				}
			}
		}

		for (final Resource resource : foundResources) {
			if (isCanceled) {
				return;
			}
			mUsedResources.add(resource);
			mResources.remove(resource);
		}
	}

	private void findDeclaredPaths(final File parent, final File file,
			final Map<String, ResourceType> resourceTypes,
			final Map<String, SortedMap<String, Resource>> resources) {
		if (file.isDirectory()) {
			for (final File child : file.listFiles()) {
				if (isCanceled) {
					return;
				}
				if (!child.isHidden()) {
					findDeclaredPaths(file, child, resourceTypes, resources);
				}
			}
		} else {
			if (!file.isHidden()) {
				final String fileName = file.getName();

				String fileContents = "";
				try {
					fileContents = FileUtils.getFileContents(file);
				} catch (final IOException e) {
					e.printStackTrace();
				}

				for (final ResourceType resourceType : resourceTypes.values()) {
					if (isCanceled) {
						return;
					}
					final Map<String, Resource> typeMap = resources
							.get(resourceType.getType());

					if (typeMap != null) {
						for (final Resource resource : typeMap.values()) {
							if (isCanceled) {
								return;
							}
							if (resourceType.doesFileDeclareResource(parent,
									fileName, fileContents, resource.getName()
											.replace("_", "[_\\.]"))) {
								resource.addDeclaredPath(file.getAbsolutePath());

								final String configuration = parent.getName();
								resource.addConfiguration(configuration);
							}
						}
					}
				}
			}
		}
	}
}
