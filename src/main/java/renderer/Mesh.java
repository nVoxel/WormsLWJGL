package renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.*;

public class Mesh {
    
    public int[] createBlockMesh(float[] blockVertices, int[] blockIndices) {
        try (MemoryStack stack = stackPush()) {
            int blockVertexArray =  GL30.glGenVertexArrays();
            GL30.glBindVertexArray(blockVertexArray);
            
            // Vertices
            FloatBuffer verticesBuffer = stackMallocFloat(blockVertices.length * 3);
            verticesBuffer.put(blockVertices);
            verticesBuffer.flip();
            int blockVertexArrayBuffer = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, blockVertexArrayBuffer);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
            GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL30.glEnableVertexAttribArray(0);
            
            // Indices
            IntBuffer indicesBuffer = stackMallocInt(blockIndices.length);
            indicesBuffer.put(blockIndices);
            indicesBuffer.flip();
            int blockElementArrayBuffer = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, blockElementArrayBuffer);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
            
            // ...
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);  // XXX: Needed?
            
            return new int[]{blockVertexArray, blockVertexArrayBuffer, blockElementArrayBuffer};
        }
    }
}
