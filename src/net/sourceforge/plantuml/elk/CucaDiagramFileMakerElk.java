/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.elk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.abel.CucaNote;
import net.sourceforge.plantuml.abel.Entity;
import net.sourceforge.plantuml.abel.GroupType;
import net.sourceforge.plantuml.abel.LeafType;
import net.sourceforge.plantuml.abel.Link;
import net.sourceforge.plantuml.abel.LinkArrow;
import net.sourceforge.plantuml.annotation.DuplicateCode;
import net.sourceforge.plantuml.api.ImageDataSimple;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.cucadiagram.ICucaDiagram;
import net.sourceforge.plantuml.decoration.symbol.USymbolFolder;
import net.sourceforge.plantuml.eggs.QuoteUtils;

/*
 * You can choose between real "org.eclipse.elk..." classes or proxied "net.sourceforge.plantuml.elk.proxy..."
 * 
 * Using proxied classes allows to compile PlantUML without having ELK available on the classpath.
 * Since GraphViz is the default layout engine up to now, we do not want to enforce the use of ELK just for compilation.
 * (for people not using maven)
 * 
 * If you are debugging, you should probably switch to "org.eclipse.elk..." classes
 * 
 */

/*
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.EdgeLabelPlacement;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.util.NullElkProgressMonitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;
*/

import net.sourceforge.plantuml.elk.proxy.core.RecursiveGraphLayoutEngine;
import net.sourceforge.plantuml.elk.proxy.core.math.ElkPadding;
import net.sourceforge.plantuml.elk.proxy.core.options.CoreOptions;
import net.sourceforge.plantuml.elk.proxy.core.options.Direction;
import net.sourceforge.plantuml.elk.proxy.core.options.EdgeLabelPlacement;
import net.sourceforge.plantuml.elk.proxy.core.options.HierarchyHandling;
import net.sourceforge.plantuml.elk.proxy.core.options.NodeLabelPlacement;
import net.sourceforge.plantuml.elk.proxy.core.util.NullElkProgressMonitor;
import net.sourceforge.plantuml.elk.proxy.graph.ElkEdge;
import net.sourceforge.plantuml.elk.proxy.graph.ElkLabel;
import net.sourceforge.plantuml.elk.proxy.graph.ElkNode;
import net.sourceforge.plantuml.elk.proxy.graph.util.ElkGraphUtil;
import net.sourceforge.plantuml.klimt.UStroke;
import net.sourceforge.plantuml.klimt.UTranslate;
import net.sourceforge.plantuml.klimt.color.HColor;
import net.sourceforge.plantuml.klimt.color.HColors;
import net.sourceforge.plantuml.klimt.creole.CreoleMode;
import net.sourceforge.plantuml.klimt.creole.Display;
import net.sourceforge.plantuml.klimt.drawing.UGraphic;
import net.sourceforge.plantuml.klimt.font.FontConfiguration;
import net.sourceforge.plantuml.klimt.font.FontParam;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.klimt.geom.HorizontalAlignment;
import net.sourceforge.plantuml.klimt.geom.MinMax;
import net.sourceforge.plantuml.klimt.geom.RectangleArea;
import net.sourceforge.plantuml.klimt.geom.VerticalAlignment;
import net.sourceforge.plantuml.klimt.geom.XDimension2D;
import net.sourceforge.plantuml.klimt.geom.XPoint2D;
import net.sourceforge.plantuml.klimt.shape.AbstractTextBlock;
import net.sourceforge.plantuml.klimt.shape.TextBlock;
import net.sourceforge.plantuml.klimt.shape.TextBlockUtils;
import net.sourceforge.plantuml.klimt.shape.URectangle;
import net.sourceforge.plantuml.log.Logme;
import net.sourceforge.plantuml.skin.AlignmentParam;
import net.sourceforge.plantuml.skin.UmlDiagramType;
import net.sourceforge.plantuml.skin.VisibilityModifier;
import net.sourceforge.plantuml.skin.rose.Rose;
import net.sourceforge.plantuml.stereo.Stereotype;
import net.sourceforge.plantuml.style.ISkinParam;
import net.sourceforge.plantuml.style.PName;
import net.sourceforge.plantuml.style.SName;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.style.StyleSignature;
import net.sourceforge.plantuml.style.StyleSignatureBasic;
import net.sourceforge.plantuml.svek.Bibliotekon;
import net.sourceforge.plantuml.svek.Cluster;
import net.sourceforge.plantuml.svek.ClusterDecoration;
import net.sourceforge.plantuml.svek.ClusterHeader;
import net.sourceforge.plantuml.svek.CucaDiagramFileMaker;
import net.sourceforge.plantuml.svek.DotStringFactory;
import net.sourceforge.plantuml.svek.GeneralImageBuilder;
import net.sourceforge.plantuml.svek.GraphvizCrash;
import net.sourceforge.plantuml.svek.IEntityImage;
import net.sourceforge.plantuml.svek.PackageStyle;
import net.sourceforge.plantuml.svek.image.EntityImageNoteLink;
import net.sourceforge.plantuml.utils.Position;

/*
 * Some notes:
 * 
https://www.eclipse.org/elk/documentation/tooldevelopers/graphdatastructure.html
https://www.eclipse.org/elk/documentation/tooldevelopers/graphdatastructure/coordinatesystem.html

Long hierarchical edge

https://rtsys.informatik.uni-kiel.de/~biblio/downloads/theses/yab-bt.pdf
https://rtsys.informatik.uni-kiel.de/~biblio/downloads/theses/thw-bt.pdf
 */
@DuplicateCode(reference = "SvekLine, CucaDiagramFileMakerElk, CucaDiagramFileMakerSmetana")
public class CucaDiagramFileMakerElk implements CucaDiagramFileMaker {
	// ::remove folder when __CORE__

	private final ICucaDiagram diagram;
	private final StringBounder stringBounder;
	private final DotStringFactory dotStringFactory;

	private final Map<Entity, ElkNode> nodes = new LinkedHashMap<Entity, ElkNode>();
	private final Map<Entity, ElkNode> clusters = new LinkedHashMap<Entity, ElkNode>();
	private final Map<Link, ElkEdge> edges = new LinkedHashMap<Link, ElkEdge>();

	public CucaDiagramFileMakerElk(ICucaDiagram diagram, StringBounder stringBounder) {
		this.diagram = diagram;
		this.stringBounder = stringBounder;
		this.dotStringFactory = new DotStringFactory(stringBounder, diagram);

	}

	// Duplicate from CucaDiagramFileMakerSmetana
	private Style getStyle() {
		return StyleSignatureBasic
				.of(SName.root, SName.element, diagram.getUmlDiagramType().getStyleName(), SName.arrow)
				.getMergedStyle(diagram.getSkinParam().getCurrentStyleBuilder());
	}

	
	// Duplication from SvekLine
	final public StyleSignature getDefaultStyleDefinitionArrow(Stereotype stereotype, SName styleName) {
		StyleSignature result = StyleSignatureBasic.of(SName.root, SName.element, styleName, SName.arrow);
		if (stereotype != null)
			result = result.withTOBECHANGED(stereotype);

		return result;
	}

	private FontConfiguration getFontForLink(Link link, final ISkinParam skinParam) {
		final SName styleName = skinParam.getUmlDiagramType().getStyleName();
		
		final Style style = getDefaultStyleDefinitionArrow(link.getStereotype(), styleName).getMergedStyle(link.getStyleBuilder());
		return style.getFontConfiguration(skinParam.getIHtmlColorSet());
	}

	private HorizontalAlignment getMessageTextAlignment(UmlDiagramType umlDiagramType, ISkinParam skinParam) {
		if (umlDiagramType == UmlDiagramType.STATE)
			return skinParam.getHorizontalAlignment(AlignmentParam.stateMessageAlignment, null, false, null);

		return skinParam.getDefaultTextAlignment(HorizontalAlignment.CENTER);
	}

	private TextBlock addVisibilityModifier(TextBlock block, Link link, ISkinParam skinParam) {
		final VisibilityModifier visibilityModifier = link.getVisibilityModifier();
		if (visibilityModifier != null) {
			final Rose rose = new Rose();
			final HColor fore = rose.getHtmlColor(skinParam, visibilityModifier.getForeground());
			TextBlock visibility = visibilityModifier.getUBlock(skinParam.classAttributeIconSize(), fore, null, false);
			visibility = TextBlockUtils.withMargin(visibility, 0, 1, 2, 0);
			block = TextBlockUtils.mergeLR(visibility, block, VerticalAlignment.CENTER);
		}
		final double marginLabel = 1; // startUid.equalsId(endUid) ? 6 : 1;
		return TextBlockUtils.withMargin(block, marginLabel, marginLabel);
	}

	private LinkArrow getLinkArrow(Link link) {
		return link.getLinkArrow();
	}


	private TextBlock getLabel(Link link) {
		ISkinParam skinParam = diagram.getSkinParam();
		final double marginLabel = 1; // startUid.equals(endUid) ? 6 : 1;

		// final FontConfiguration labelFont =
		// style.getFontConfiguration(skinParam.getIHtmlColorSet());
//		TextBlock labelOnly = link.getLabel().create(labelFont,
//				skinParam.getDefaultTextAlignment(HorizontalAlignment.CENTER), skinParam);

		final UmlDiagramType type = skinParam.getUmlDiagramType();
		final FontConfiguration font = getFontForLink(link, skinParam);

		TextBlock labelOnly;
		// toto2
		if (Display.isNull(link.getLabel())) {
			labelOnly = TextBlockUtils.EMPTY_TEXT_BLOCK;
			if (getLinkArrow(link) != LinkArrow.NONE_OR_SEVERAL) {
				// labelOnly = StringWithArrow.addMagicArrow(labelOnly, this, font);
			}

		} else {
			final HorizontalAlignment alignment = getMessageTextAlignment(type, skinParam);
			final boolean hasSeveralGuideLines = link.getLabel().hasSeveralGuideLines();
			final TextBlock block;
			// if (hasSeveralGuideLines)
				// block = StringWithArrow.addSeveralMagicArrows(link.getLabel(), this, font, alignment, skinParam);
			// else
				block = link.getLabel().create0(font, alignment, skinParam, skinParam.maxMessageSize(),
						CreoleMode.SIMPLE_LINE, null, null);

			labelOnly = addVisibilityModifier(block, link, skinParam);
			if (getLinkArrow(link) != LinkArrow.NONE_OR_SEVERAL && hasSeveralGuideLines == false) {
				// labelOnly = StringWithArrow.addMagicArrow(labelOnly, this, font);
			}

		}

		final CucaNote note = link.getNote();
		if (note == null) {
			if (TextBlockUtils.isEmpty(labelOnly, stringBounder) == false)
				labelOnly = TextBlockUtils.withMargin(labelOnly, marginLabel, marginLabel);
			return labelOnly;
		}
		final TextBlock noteOnly = new EntityImageNoteLink(note.getDisplay(), note.getColors(), skinParam,
				link.getStyleBuilder());

		if (note.getPosition() == Position.LEFT)
			return TextBlockUtils.mergeLR(noteOnly, labelOnly, VerticalAlignment.CENTER);
		else if (note.getPosition() == Position.RIGHT)
			return TextBlockUtils.mergeLR(labelOnly, noteOnly, VerticalAlignment.CENTER);
		else if (note.getPosition() == Position.TOP)
			return TextBlockUtils.mergeTB(noteOnly, labelOnly, HorizontalAlignment.CENTER);
		else
			return TextBlockUtils.mergeTB(labelOnly, noteOnly, HorizontalAlignment.CENTER);

	}

	private TextBlock getQuantifier(Link link, int n) {
		final String tmp = n == 1 ? link.getQuantifier1() : link.getQuantifier2();
		if (tmp == null)
			return null;

		final ISkinParam skinParam = diagram.getSkinParam();
		final FontConfiguration labelFont = FontConfiguration.create(skinParam, FontParam.ARROW, null);
		final TextBlock label = Display.getWithNewlines(tmp).create(labelFont,
				skinParam.getDefaultTextAlignment(HorizontalAlignment.CENTER), skinParam);
		if (TextBlockUtils.isEmpty(label, stringBounder))
			return null;

		return label;
	}

	// Retrieve the real position of a node, depending on its parents
	private XPoint2D getPosition(ElkNode elkNode) {
		final ElkNode parent = elkNode.getParent();

		final double x = elkNode.getX();
		final double y = elkNode.getY();

		// This nasty test checks that parent is "root"
		if (parent == null || parent.getLabels().size() == 0) {
			return new XPoint2D(x, y);
		}

		// Right now, this is recursive
		final XPoint2D parentPosition = getPosition(parent);
		return new XPoint2D(parentPosition.getX() + x, parentPosition.getY() + y);

	}

	// The Drawing class does the real drawing
	class Drawing extends AbstractTextBlock {

		// min and max of all coord
		private final MinMax minMax;

		public Drawing(MinMax minMax) {
			this.minMax = minMax;
		}

		public void drawU(UGraphic ug) {
			drawAllClusters(ug);
			drawAllNodes(ug);
			drawAllEdges(ug);
		}

		private void drawAllClusters(UGraphic ug) {
			for (Entry<Entity, ElkNode> ent : clusters.entrySet())
				drawSingleCluster(ug, ent.getKey(), ent.getValue());

		}

		private void drawAllNodes(UGraphic ug) {
			for (Entry<Entity, ElkNode> ent : nodes.entrySet())
				drawSingleNode(ug, ent.getKey(), ent.getValue());

		}

		private void drawAllEdges(UGraphic ug) {
			for (Entry<Link, ElkEdge> ent : edges.entrySet()) {
				final Link link = ent.getKey();
				if (link.isInvis())
					continue;

				drawSingleEdge(ug, link, ent.getValue());
			}
		}

		private void drawSingleCluster(UGraphic ug, Entity group, ElkNode elkNode) {
			final XPoint2D corner = getPosition(elkNode);
			final URectangle rect = URectangle.build(elkNode.getWidth(), elkNode.getHeight());

			PackageStyle packageStyle = group.getPackageStyle();
			final ISkinParam skinParam = diagram.getSkinParam();
			if (packageStyle == null)
				packageStyle = skinParam.packageStyle();

			final UmlDiagramType umlDiagramType = diagram.getUmlDiagramType();

			final Style style = Cluster
					.getDefaultStyleDefinition(umlDiagramType.getStyleName(), group.getUSymbol(), group.getGroupType())
					.getMergedStyle(skinParam.getCurrentStyleBuilder());
			final double shadowing = style.value(PName.Shadowing).asDouble();
			final UStroke stroke = Cluster.getStrokeInternal(group, style);

			HColor backColor = getBackColor(umlDiagramType);
			backColor = Cluster.getBackColor(backColor, group.getStereotype(), umlDiagramType.getStyleName(),
					group.getUSymbol(), skinParam.getCurrentStyleBuilder(), skinParam.getIHtmlColorSet(),
					group.getGroupType());

			final double roundCorner = style.value(PName.RoundCorner).asDouble();
//			final double roundCorner = group.getUSymbol() == null ? 0
//					: group.getUSymbol().getSkinParameter().getRoundCorner(skinParam, group.getStereotype());

			final RectangleArea rectangleArea = new RectangleArea(0, 0, elkNode.getWidth(), elkNode.getHeight());
			final ClusterHeader clusterHeader = new ClusterHeader(group, diagram.getSkinParam(), diagram,
					stringBounder);

			final ClusterDecoration decoration = new ClusterDecoration(packageStyle, group.getUSymbol(),
					clusterHeader.getTitle(), clusterHeader.getStereo(), rectangleArea, stroke);

			final HColor borderColor = HColors.BLACK;
			decoration.drawU(ug.apply(UTranslate.point(corner)), backColor, borderColor, shadowing, roundCorner,
					skinParam.getHorizontalAlignment(AlignmentParam.packageTitleAlignment, null, false, null),
					skinParam.getStereotypeAlignment(), 0);

//			// Print a simple rectangle right now
//			ug.apply(HColorUtils.BLACK).apply(UStroke.withThickness(1.5)).apply(new UTranslate(corner)).draw(rect);
		}

		private HColor getBackColor(UmlDiagramType umlDiagramType) {
			return null;
		}

		private void drawSingleNode(UGraphic ug, Entity leaf, ElkNode elkNode) {
			final IEntityImage image = printEntityInternal(leaf);
			// Retrieve coord from ELK
			final XPoint2D corner = getPosition(elkNode);

			// Print the node image at right coord
			image.drawU(ug.apply(UTranslate.point(corner)));
		}

		private void drawSingleEdge(UGraphic ug, Link link, ElkEdge edge) {
			// Unfortunately, we have to translate "edge" in its own "cluster" coordinate
			final XPoint2D translate = getPosition(edge.getContainingNode());

			final double magicY2 = 0;
			final Entity dest = link.getEntity2();
			if (dest.getUSymbol() instanceof USymbolFolder) {
//				System.err.println("dest=" + dest);
//				final IEntityImage image = printEntityInternal((ILeaf) dest);
//				System.err.println("image=" + image);

			}
			final ElkPath elkPath = new ElkPath(diagram, SName.classDiagram, link, edge, getLabel(link),
					getQuantifier(link, 1), getQuantifier(link, 2), magicY2);
			elkPath.drawU(ug.apply(UTranslate.point(translate)));
		}

		public XDimension2D calculateDimension(StringBounder stringBounder) {
			if (minMax == null)
				throw new UnsupportedOperationException();

			return minMax.getDimension();
		}

		public HColor getBackcolor() {
			return null;
		}

	}

	private Collection<Entity> getUnpackagedEntities() {
		final List<Entity> result = new ArrayList<>();
		for (Entity ent : diagram.getEntityFactory().leafs())
			if (diagram.getEntityFactory().getRootGroup() == ent.getParentContainer())
				result.add(ent);

		return result;
	}

	private ElkNode getElkNode(final Entity entity) {
		ElkNode node = nodes.get(entity);
		if (node == null)
			node = clusters.get(entity);

		return node;
	}

	@Override
	public ImageData createFile(OutputStream os, List<String> dotStrings, FileFormatOption fileFormatOption)
			throws IOException {

		// https://www.eclipse.org/forums/index.php/t/1095737/
		try {
			final ElkNode root = ElkGraphUtil.createGraph();
			root.setProperty(CoreOptions.DIRECTION, Direction.DOWN);
			root.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.INCLUDE_CHILDREN);

			printAllSubgroups(root, diagram.getRootGroup());
			printEntities(root, getUnpackagedEntities());

			manageAllEdges();

			new RecursiveGraphLayoutEngine().layout(root, new NullElkProgressMonitor());

			final MinMax minMax = TextBlockUtils.getMinMax(new Drawing(null), stringBounder, false);

			final TextBlock drawable = new Drawing(minMax);
			return diagram.createImageBuilder(fileFormatOption) //
					.drawable(drawable) //
					.write(os); //

		} catch (Throwable e) {
			UmlDiagram.exportDiagramError(os, e, fileFormatOption, diagram.seed(), diagram.getMetadata(),
					diagram.getFlashData(), getFailureText3(e));
			return ImageDataSimple.error();
		}

	}

	private void printAllSubgroups(ElkNode cluster, Entity group) {
		for (Entity g : diagram.getChildrenGroups(group)) {
			if (g.isRemoved()) {
				continue;
			}
			if (diagram.isEmpty(g) && g.getGroupType() == GroupType.PACKAGE) {
				g.muteToType(LeafType.EMPTY_PACKAGE);
				manageSingleNode(cluster, g);
			} else {

				// We create the "cluster" in ELK for this group
				final ElkNode elkCluster = ElkGraphUtil.createNode(cluster);
				elkCluster.setProperty(CoreOptions.DIRECTION, Direction.DOWN);

				final ClusterHeader clusterHeader = new ClusterHeader(g, diagram.getSkinParam(), diagram,
						stringBounder);

				final int titleAndAttributeHeight = clusterHeader.getTitleAndAttributeHeight();

				final double topPadding = Math.max(25, titleAndAttributeHeight) + 15;
				elkCluster.setProperty(CoreOptions.PADDING, new ElkPadding(topPadding, 15, 15, 15));

				// Not sure this is usefull to put a label on a "cluster"
				final ElkLabel label = ElkGraphUtil.createLabel(elkCluster);
				label.setText("C");
				// We need it anyway to recurse up to the real "root"

				this.clusters.put(g, elkCluster);
				printSingleGroup(g);
			}
		}

	}

	private void printSingleGroup(Entity g) {
		if (g.getGroupType() == GroupType.CONCURRENT_STATE)
			return;

		this.printEntities(clusters.get(g), g.leafs());
		printAllSubgroups(clusters.get(g), g);
	}

	private void printEntities(ElkNode parent, Collection<Entity> entities) {
		// Convert all "leaf" to ELK node
		for (Entity ent : entities) {
			if (ent.isRemoved())
				continue;

			manageSingleNode(parent, ent);
		}
	}

	private void manageAllEdges() {
		// Convert all "link" to ELK edge
		for (final Link link : diagram.getLinks())
			manageSingleEdge(link);

	}

	private void manageSingleNode(ElkNode parent, Entity leaf) {
		final IEntityImage image = printEntityInternal(leaf);

		// Expected dimension of the node
		final XDimension2D dimension = image.calculateDimension(stringBounder);

		// Here, we try to tell ELK to use this dimension as node dimension
		final ElkNode node = ElkGraphUtil.createNode(parent);
		node.setDimensions(dimension.getWidth(), dimension.getHeight());

		// There is no real "label" here
		// We just would like to force node dimension
		final ElkLabel label = ElkGraphUtil.createLabel(node);
		label.setText("X");

		// I don't know why we have to do this hack, but somebody has to fix it
		final double VERY_STRANGE_OFFSET = 10;
		label.setDimensions(dimension.getWidth(), dimension.getHeight() - VERY_STRANGE_OFFSET);

		// No idea of what we are doing here :-)
		label.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
				EnumSet.of(NodeLabelPlacement.INSIDE, NodeLabelPlacement.H_CENTER, NodeLabelPlacement.V_CENTER));

		// This padding setting have no impact ?
		// label.setProperty(CoreOptions.NODE_LABELS_PADDING, new ElkPadding(100.0));

		// final EnumSet<SizeConstraint> constraints =
		// EnumSet.of(SizeConstraint.NODE_LABELS);
		// node.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, constraints);

		// node.setProperty(CoreOptions.NODE_SIZE_OPTIONS,
		// EnumSet.noneOf(SizeOptions.class));

		// Let's store this
		nodes.put(leaf, node);
	}

	private void manageSingleEdge(final Link link) {
		final ElkNode node1 = getElkNode(link.getEntity1());
		final ElkNode node2 = getElkNode(link.getEntity2());

		final ElkEdge edge = ElkGraphUtil.createSimpleEdge(node1, node2);

		final TextBlock labelLink = getLabel(link);
		if (labelLink != null) {
			final ElkLabel edgeLabel = ElkGraphUtil.createLabel(edge);
			final XDimension2D dim = labelLink.calculateDimension(stringBounder);
			edgeLabel.setText("X");
			edgeLabel.setDimensions(dim.getWidth(), dim.getHeight());
			// Duplicated, with qualifier, but who cares?
			edge.setProperty(CoreOptions.EDGE_LABELS_INLINE, true);
			// edge.setProperty(CoreOptions.EDGE_TYPE, EdgeType.ASSOCIATION);
		}
		if (link.getQuantifier1() != null) {
			final ElkLabel edgeLabel = ElkGraphUtil.createLabel(edge);
			final XDimension2D dim = getQuantifier(link, 1).calculateDimension(stringBounder);
			// Nasty trick, we store the kind of label in the text
			edgeLabel.setText("1");
			edgeLabel.setDimensions(dim.getWidth(), dim.getHeight());
			edgeLabel.setProperty(CoreOptions.EDGE_LABELS_PLACEMENT, EdgeLabelPlacement.TAIL);
			// Duplicated, with main label, but who cares?
			edge.setProperty(CoreOptions.EDGE_LABELS_INLINE, true);
			// edge.setProperty(CoreOptions.EDGE_TYPE, EdgeType.ASSOCIATION);
		}
		if (link.getQuantifier2() != null) {
			final ElkLabel edgeLabel = ElkGraphUtil.createLabel(edge);
			final XDimension2D dim = getQuantifier(link, 2).calculateDimension(stringBounder);
			// Nasty trick, we store the kind of label in the text
			edgeLabel.setText("2");
			edgeLabel.setDimensions(dim.getWidth(), dim.getHeight());
			edgeLabel.setProperty(CoreOptions.EDGE_LABELS_PLACEMENT, EdgeLabelPlacement.HEAD);
			// Duplicated, with main label, but who cares?
			edge.setProperty(CoreOptions.EDGE_LABELS_INLINE, true);
			// edge.setProperty(CoreOptions.EDGE_TYPE, EdgeType.ASSOCIATION);
		}

		edges.put(link, edge);
	}

	static private List<String> getFailureText3(Throwable exception) {
		Logme.error(exception);
		final List<String> strings = new ArrayList<>();
		strings.add("An error has occured : " + exception);
		final String quote = StringUtils.rot(QuoteUtils.getSomeQuote());
		strings.add("<i>" + quote);
		strings.add(" ");
		GraphvizCrash.addProperties(strings);
		strings.add(" ");
		strings.add("Sorry, ELK intregration is really alpha feature...");
		strings.add(" ");
		strings.add("You should send this diagram and this image to <b>plantuml@gmail.com</b> or");
		strings.add("post to <b>https://plantuml.com/qa</b> to solve this issue.");
		strings.add(" ");
		return strings;
	}

	private Bibliotekon getBibliotekon() {
		return dotStringFactory.getBibliotekon();
	}

	private IEntityImage printEntityInternal(Entity ent) {
		if (ent.isRemoved())
			throw new IllegalStateException();

		if (ent.getSvekImage() == null) {
			final ISkinParam skinParam = diagram.getSkinParam();
			if (skinParam.sameClassWidth())
				System.err.println("NOT YET IMPLEMENED");

			return GeneralImageBuilder.createEntityImageBlock(ent, skinParam, diagram.isHideEmptyDescriptionForState(),
					diagram, getBibliotekon(), null, diagram.getUmlDiagramType(), diagram.getLinks());
		}
		return ent.getSvekImage();
	}

	@Override
	public void createOneGraphic(UGraphic ug) {
		throw new UnsupportedOperationException();
	}

}
