package sat;

import com.microsoft.z3.IntExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import java.io.*;
import java.util.*;

public class GraphColoring {

    public static void main(String[] args) {
        try {
            // Create a Z3 context and solver.
            Context ctx = new Context();
            Solver solver = ctx.mkSolver();

            String inPath = args[0];
            String outPath = args[1];

            int numVertices = 0;
            int numColours = 0;
            
            // start the process of reading each of the nodes.
            List<Edge> edges = new ArrayList<>();
            try (Scanner scanner = new Scanner(new File(inPath))) {
                // the first row of the input can be assumed to be the number of nodes and colours.
                numVertices = scanner.nextInt();
                numColours = scanner.nextInt();

                while (scanner.hasNextInt()) {
                    int newU = scanner.nextInt();
                    int newV = scanner.nextInt();
                    Edge newEdge = new Edge(newU, newV);
                    edges.add(newEdge);
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + inPath);
                e.printStackTrace();
            }

            System.out.println(edges);

            // declare the array that will correspond to color(v) = c_x.
            IntExpr[] colour = new IntExpr[numVertices + 1];

            // add constraints for each of the vertices; ensuring the colour is 1 <= v <= M. this means
            // that each vertex from 1 <= i <= n has a colour (if there is no colour, their colour will be null)
            for (int i = 1; i <= numVertices; i++) {
                colour[i] = ctx.mkIntConst("colour_" + i);

                // (colour(i) >= 1) /\ (colour(i) <= numColours)
                BoolExpr colourNumberConstraint = ctx.mkAnd(
                    ctx.mkGe(colour[i], ctx.mkInt(1)),
                    ctx.mkLe(colour[i], ctx.mkInt(numColours))
                );
                solver.add(colourNumberConstraint);
            }

            // add constraints for each edge to ensure the vertices which they are connecting are not the same colour.
            for (Edge edge : edges) {
                // (colour(u) != colour(v))
                BoolExpr edgeColourConstraint = ctx.mkNot(ctx.mkEq(colour[edge.getU()], colour[edge.getV()]));
                solver.add(edgeColourConstraint);
            }

            if (solver.check() == Status.SATISFIABLE) {
                System.out.println("SAT");
                Model model = solver.getModel();
                try {
                    FileWriter fw = new FileWriter(outPath);
                    for (int i = 1; i <= numVertices; i++) {
                        Expr colourVal = model.getConstInterp(colour[i]);
                        fw.write(i + " " + colourVal + "\n");
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileWriter fw = new FileWriter(outPath);
                    fw.write("No Solution\n");
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ctx.close();
        } catch (Z3Exception e) {
            e.printStackTrace();
        }
    }

    public static class Edge {
        private final int u;
        private final int v;

        public Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }

        private int getU() {
            return this.u;
        }

        private int getV() {
            return this.v;
        }

        @Override
        public String toString() {
            return u + " - " + v;
        }
    }
}
