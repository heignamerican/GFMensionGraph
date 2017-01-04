package heignamerican.gf.mentiongraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.CenterEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * 参考
 * https://blog.guildworks.jp/2014/10/16/effective-java-visualization/
 */
public class Visualization {
    private static final Rectangle RECTANGLE = new Rectangle(120, 20);

    private static Graph<String, Supplier<Integer>> createGraph() throws IOException {
        // 項目同士の関係性を有向グラフで表す
        final Graph<String, Supplier<Integer>> graph = new DirectedSparseGraph<>();

        // 以下の形式のファイルを用意する (mention.txt)
        //
        // line ::= source <\t> target <\t> edgeLabel
        // ...
        final List<String> lines = Files.readAllLines(Paths.get("mentions.txt"), StandardCharsets.UTF_8);
        for (final String line : lines) {
            if (line != null && !line.isEmpty()) {
                final String[] vals = line.split("\t");
                if (vals.length != 3) {
                    throw new IllegalArgumentException(line);
                }

                final String source = vals[0];
                final String target = vals[1];
                final Integer edgeLabel = Integer.valueOf(vals[2]);
                if (graph.containsVertex(source) == false) {
                    graph.addVertex(source);
                }
                if (graph.containsVertex(target) == false) {
                    graph.addVertex(target);
                }
                graph.addEdge(() -> edgeLabel, source, target);
            }
        }
        return graph;
    }

    private static void visualize(final Graph<String, Supplier<Integer>> graph) {
        //        OptionalInt max = graph.getEdges().stream().mapToInt(s -> s.get()).max();
        //        OptionalInt min = graph.getEdges().stream().mapToInt(s -> s.get()).min();

        final int width = 1800;
        final int height = 1080;

        // the Fruchterman-Reingold force-directed algorithm
        final KKLayout<String, Supplier<Integer>> layout = new KKLayout<>(graph);
        layout.setExchangeVertices(false);
        layout.setDisconnectedDistanceMultiplier(0.68);

        layout.setSize(new Dimension(width, height));
        final BasicVisualizationServer<String, Supplier<Integer>> visualizationServer = new BasicVisualizationServer<>(layout);
        visualizationServer.setPreferredSize(new Dimension(width, height));

        visualizationServer.getRenderContext()
                .setVertexShapeTransformer(x -> RECTANGLE);
        visualizationServer.getRenderContext()
                .setVertexFillPaintTransformer(v -> Color.YELLOW);
        visualizationServer.getRenderContext()
                .setVertexLabelTransformer(new ToStringLabeller<>());
        visualizationServer.getRenderer()
                .getVertexLabelRenderer()
                .setPosition(Renderer.VertexLabel.Position.CNTR);
        visualizationServer.getRenderer()
                .getEdgeRenderer()
                .setEdgeArrowRenderingSupport(new CenterEdgeArrowRenderingSupport<>());

        visualizationServer.getRenderContext()
                .setEdgeStrokeTransformer(s -> new BasicStroke((s.get() - 6) / 2F));
        visualizationServer.getRenderContext()
                .setEdgeArrowStrokeTransformer(s -> new BasicStroke((s.get() - 6) / 2F));
        visualizationServer.getRenderContext()
                .setEdgeDrawPaintTransformer(e -> Color.CYAN);
        visualizationServer.getRenderContext()
                .setEdgeLabelTransformer(s -> String.valueOf(s.get()));

        //visualizationServer.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING);

        final JFrame frame = new JFrame("ヒトコト");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Component component;
        {
            JPanel panel = new JPanel();
            panel.add(visualizationServer);
            panel.setSize(width, height);
            JScrollPane comp = new JScrollPane(panel);
            comp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            component = visualizationServer;
        }

        frame.add(component);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String... args) throws IOException {
        visualize(createGraph());
    }
}
