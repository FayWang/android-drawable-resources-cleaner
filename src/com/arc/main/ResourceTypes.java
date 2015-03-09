package com.arc.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arc.model.ResourceType;

/**
 * 构造资源类型
 * @author xiaofei9
 * 2015-1-30 下午6:56:30
 */

public class ResourceTypes {
	public static final Map<String, ResourceType> sResourceTypes = new HashMap<String, ResourceType>();
	
	public ResourceTypes() {
		// TODO Auto-generated constructor stub
	}
	
	static {
		// anim
		sResourceTypes.put("anim", new ResourceType("anim") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals(getType())) {
					return false;
				}

				final String name = fileName.split("\\.")[0];
				System.out.println("anim parent: " + parent);
				System.out.println("anim fileName: " + fileName);
				System.out.println("anim name: " + name);
				System.out.println("anim resourceName: " + resourceName);
				final Pattern pattern = Pattern.compile("^" + resourceName
						+ "$");
				return pattern.matcher(name).find();
			}
		});

		// array
		sResourceTypes.put("array", new ResourceType("array") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<([a-z]+\\-)?array.*?name\\s*=\\s*\""
								+ resourceName + "\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// attr
		sResourceTypes.put("attr", new ResourceType("attr") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<attr.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}

			@Override
			public boolean doesFileUseResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (parent != null) {
					if (!parent.isDirectory()) {
						return false;
					}

					final String directoryType = parent.getName().split("-")[0];
					if (!directoryType.equals("layout")
							&& !directoryType.equals("values")) {
						return false;
					}
				}

				final Pattern pattern = Pattern.compile("<.+?:" + resourceName
						+ "\\s*=\\s*\".*?\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				final Pattern itemPattern = Pattern
						.compile("<item.+?name\\s*=\\s*\"" + resourceName
								+ "\".*?>");
				final Matcher itemMatcher = itemPattern.matcher(fileContents);

				if (itemMatcher.find()) {
					return true;
				}

				return false;
			}
		});

		// bool
		sResourceTypes.put("bool", new ResourceType("bool") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<bool.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// color
		sResourceTypes.put("color", new ResourceType("color") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<color.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// dimen
		sResourceTypes.put("dimen", new ResourceType("dimen") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<dimen.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// drawable
		sResourceTypes.put("drawable", new ResourceType("drawable") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (directoryType.equals(getType())) {

					final String name = fileName.split("\\.")[0];
					final Pattern pattern = Pattern.compile("^" + resourceName
							+ "$");
					return pattern.matcher(name).find();
				}

				if (directoryType.equals("values")) {
					final Pattern pattern = Pattern
							.compile("<drawable.*?name\\s*=\\s*\""
									+ resourceName + "\".*?/?>");
					final Matcher matcher = pattern.matcher(fileContents);
					if (matcher.find()) {
						return true;
					}
				}

				return false;
			}
		});

		// id
		sResourceTypes.put("id", new ResourceType("id") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")
						&& !directoryType.equals("layout")) {
					return false;
				}

				final Pattern valuesPattern0 = Pattern
						.compile("<item.*?type\\s*=\\s*\"id\".*?name\\s*=\\s*\""
								+ resourceName + "\".*?/?>");
				final Pattern valuesPattern1 = Pattern
						.compile("<item.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?type\\s*=\\s*\"id\".*?/?>");
				final Pattern layoutPattern = Pattern
						.compile(":id\\s*=\\s*\"@\\+id/" + resourceName + "\"");
				Matcher matcher = valuesPattern0.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				matcher = valuesPattern1.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				matcher = layoutPattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// integer
		sResourceTypes.put("integer", new ResourceType("integer") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<integer.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");
				final Matcher matcher = pattern.matcher(fileContents);
				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// layout
		sResourceTypes.put("layout", new ResourceType("layout") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals(getType())) {
					return false;
				}

				final String name = fileName.split("\\.")[0];

				final Pattern pattern = Pattern.compile("^" + resourceName
						+ "$");

				return pattern.matcher(name).find();
			}
		});

		// menu
		sResourceTypes.put("menu", new ResourceType("menu") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals(getType())) {
					return false;
				}

				final String name = fileName.split("\\.")[0];
				final Pattern pattern = Pattern.compile("^" + resourceName
						+ "$");
				return pattern.matcher(name).find();
			}
		});

		// plurals
		sResourceTypes.put("plurals", new ResourceType("plurals") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<plurals.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");
				final Matcher matcher = pattern.matcher(fileContents);
				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// raw
		sResourceTypes.put("raw", new ResourceType("raw") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals(getType())) {
					return false;
				}

				final String name = fileName.split("\\.")[0];
				final Pattern pattern = Pattern.compile("^" + resourceName
						+ "$");
				return pattern.matcher(name).find();
			}
		});

		// string
		sResourceTypes.put("string", new ResourceType("string") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<string.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");
				final Matcher matcher = pattern.matcher(fileContents);
				if (matcher.find()) {
					return true;
				}

				return false;
			}
		});

		// style
		sResourceTypes.put("style", new ResourceType("style") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final Pattern pattern = Pattern
						.compile("<style.*?name\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");
				final Matcher matcher = pattern.matcher(fileContents);
				if (matcher.find()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean doesFileUseResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (parent != null) {
					if (!parent.isDirectory()) {
						return false;
					}

					final String directoryType = parent.getName().split("-")[0];
					if (!directoryType.equals("values")) {
						return false;
					}
				}

				// (name="Parent.Child")
				final Pattern pattern = Pattern
						.compile("<style.*?name\\s*=\\s*\"" + resourceName
								+ "\\.\\w+\".*?/?>");

				final Matcher matcher = pattern.matcher(fileContents);

				if (matcher.find()) {
					return true;
				}

				// (parent="Parent")
				final Pattern pattern1 = Pattern
						.compile("<style.*?parent\\s*=\\s*\"" + resourceName
								+ "\".*?/?>");
				final Matcher matcher1 = pattern1.matcher(fileContents);
				if (matcher1.find()) {
					return true;
				}
				return false;
			}
		});

		// styleable
		sResourceTypes.put("styleable", new ResourceType("styleable") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals("values")) {
					return false;
				}

				final String[] styleableAttr = resourceName
						.split("\\[_\\\\.\\]");

				if (styleableAttr.length == 1) {

					final Pattern pattern = Pattern
							.compile("<declare-styleable.*?name\\s*=\\s*\""
									+ styleableAttr[0] + "\".*?/?>");
					final Matcher matcher = pattern.matcher(fileContents);

					if (matcher.find()) {
						return true;
					}

					return false;
				}

				final Pattern blockPattern = Pattern
						.compile("<declare-styleable.*?name\\s*=\\s*\""
								+ styleableAttr[0]
								+ "\".*?>(.*?)</declare-styleable\\s*>");
				final Matcher blockMatcher = blockPattern.matcher(fileContents);

				if (blockMatcher.find()) {
					final String styleableAttributes = blockMatcher.group(1);

					final Pattern attributePattern = Pattern
							.compile("<attr.*?name\\s*=\\s*\""
									+ styleableAttr[1] + "\".*?/?>");
					final Matcher attributeMatcher = attributePattern
							.matcher(styleableAttributes);
					if (attributeMatcher.find()) {
						return true;
					}
					return false;
				}

				return false;
			}
		});

		// xml
		sResourceTypes.put("xml", new ResourceType("xml") {
			@Override
			public boolean doesFileDeclareResource(final File parent,
					final String fileName, final String fileContents,
					final String resourceName) {
				if (!parent.isDirectory()) {
					return false;
				}

				final String directoryType = parent.getName().split("-")[0];
				if (!directoryType.equals(getType())) {
					return false;
				}

				final String name = fileName.split("\\.")[0];
				final Pattern pattern = Pattern.compile("^" + resourceName
						+ "$");
				return pattern.matcher(name).find();
			}
		});
	}

}
