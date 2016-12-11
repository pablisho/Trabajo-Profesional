package ar.uba.fi.prm.arbuy.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;

import ar.uba.fi.prm.arbuy.opengl.Object3D;

public class LoaderOBJ {
    private static final String TAG = "LoaderOBJ";

	protected final String VERTEX = "v";
    protected final String FACE = "f";
    protected final String TEXCOORD = "vt";
    protected final String NORMAL = "vn";
    protected final String OBJECT = "o";
    protected final String GROUP = "g";
    protected final String MATERIAL_LIB = "mtllib";
    protected final String USE_MATERIAL = "usemtl";
    protected final String NEW_MATERIAL = "newmtl";
    protected final String DIFFUSE_COLOR = "Kd";
    protected final String DIFFUSE_TEX_MAP = "map_Kd";

    private boolean mNeedToRenameMtl = true;

	protected Resources mResources;
	protected int mResourceId;
	protected String mFileOnSDCard;
	protected File mFile;
	protected int mTag;


    /*public LoaderOBJ(Context context, String fileOnSDCard) {
    	super(renderer, fileOnSDCard);

        mNeedToRenameMtl = false;
    }*/

	public LoaderOBJ(Context context, File file) {
		mFile = file;
	}

    public LoaderOBJ(Context context, int resource){
        mResourceId = resource;
        mResources = context.getResources();
    }

	public List<Object3D> parse() throws ParsingException {
		BufferedReader buffer = null;
		if(mFile == null) {
			InputStream fileIn = mResources.openRawResource(mResourceId);
			buffer = new BufferedReader(new InputStreamReader(fileIn));
		} else {
			try {
				buffer = new BufferedReader(new FileReader(mFile));
			} catch (FileNotFoundException e) {
				Log.e(TAG, "[" + getClass().getCanonicalName() + "] Could not find file.");
				e.printStackTrace();
			}
		}
		String line;
		ObjIndexData currObjIndexData = new ObjIndexData(new Object3D());
		ArrayList<ObjIndexData> objIndices = new ArrayList<ObjIndexData>();

		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> texCoords = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		MaterialLib matLib = new MaterialLib();

		String currentMaterialName=null;
		boolean currentObjHasFaces=false;

        List<Object3D> parsedObjects = new ArrayList<>();

		try {
			while((line = buffer.readLine()) != null) {
				// Skip comments and empty lines.
				if(line.length() == 0 || line.charAt(0) == '#')
					continue;
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();

				if(numTokens == 0)
					continue;
				String type = parts.nextToken();

				if(type.equals(VERTEX)) {
					vertices.add(Float.parseFloat(parts.nextToken()));
					vertices.add(Float.parseFloat(parts.nextToken()));
					vertices.add(Float.parseFloat(parts.nextToken()));
				} else if(type.equals(FACE)) {
					currentObjHasFaces=true;
					boolean isQuad = numTokens == 5;
					int[] quadvids = new int[4];
					int[] quadtids = new int[4];
					int[] quadnids = new int[4];

                    boolean emptyVt = line.indexOf("//") > -1;
                    if(emptyVt) line = line.replace("//", "/");

                    parts = new StringTokenizer(line);

                    parts.nextToken();
                    StringTokenizer subParts = new StringTokenizer(parts.nextToken(), "/");
                    int partLength = subParts.countTokens();

                    boolean hasuv = partLength >= 2 && !emptyVt;
                    boolean hasn = partLength == 3 || (partLength == 2 && emptyVt);
                    int idx;

                    for (int i = 1; i < numTokens; i++) {
                    	if(i > 1)
                    		subParts = new StringTokenizer(parts.nextToken(), "/");
                    	idx = Integer.parseInt(subParts.nextToken());

                    	if(idx < 0) idx = (vertices.size() / 3) + idx;
                    	else idx -= 1;
                        if(!isQuad)
                        	currObjIndexData.vertexIndices.add(idx);
                        else
                        	quadvids[i-1] = idx;
                        if (hasuv)
                        {
                        	idx = Integer.parseInt(subParts.nextToken());
                        	if(idx < 0) idx = (texCoords.size() / 2) + idx;
                        	else idx -= 1;
                        	if(!isQuad)
                        		currObjIndexData.texCoordIndices.add(idx);
                        	else
                            	quadtids[i-1] = idx;
                        }
                        if (hasn)
                        {
                        	idx = Integer.parseInt(subParts.nextToken());
                        	if(idx < 0) idx = (normals.size() / 3) + idx;
                        	else idx -= 1;
                        	if(!isQuad)
                        		currObjIndexData.normalIndices.add(idx);
                        	else
                            	quadnids[i-1] = idx;
                        }
                    }

                    if(isQuad) {
                    	int[] indices = new int[] { 0, 1, 2, 0, 2, 3 };

                    	for(int i=0; i<6; ++i) {
                    		int index = indices[i];
                        	currObjIndexData.vertexIndices.add(quadvids[index]);
                        	currObjIndexData.texCoordIndices.add(quadtids[index]);
                        	currObjIndexData.normalIndices.add(quadnids[index]);
                    	}
                    }
				} else if(type.equals(TEXCOORD)) {
					texCoords.add(Float.parseFloat(parts.nextToken()));
                    texCoords.add(1f - Float.parseFloat(parts.nextToken()));
				} else if(type.equals(NORMAL)) {
					normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
				}/* else if(type.equals(GROUP)) {
					int numGroups = parts.countTokens();
					Object3D previousGroup = null;
					for(int i=0; i<numGroups; i++) {
						String groupName = parts.nextToken();
						if(!groups.containsKey(groupName)) {
							groups.put(groupName, new Object3D(groupName));
						}
						Object3D group = groups.get(groupName);
						if(previousGroup!=null) {
							addChildSetParent(group, previousGroup);
						} else {
							currentGroup = group;
						}
						previousGroup = group;
					}
					RajLog.i("Parsing group: " + currentGroup.getName());
					if (currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D(generateObjectName()));
						RajLog.i("Parsing object: " + currObjIndexData.targetObj.getName());
						currObjIndexData.materialName = currentMaterialName;
						currentObjHasFaces = false;
					}
					addChildSetParent(currentGroup, currObjIndexData.targetObj);
				} */else if(type.equals(OBJECT)) {
					String objName = parts.hasMoreTokens() ? parts.nextToken() : generateObjectName();

					if (currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D());
						currObjIndexData.materialName = currentMaterialName;
						//addChildSetParent(currentGroup, currObjIndexData.targetObj);
						//Log.i(TAG, "Parsing object: " + currObjIndexData.targetObj.getName());
						currentObjHasFaces = false;
					}
					//currObjIndexData.targetObj.setName(objName);
				} else if(type.equals(MATERIAL_LIB)) {
					if(!parts.hasMoreTokens()) continue;
                    String materialLibPath = mNeedToRenameMtl ? parts.nextToken().replace(".", "_") : parts.nextToken();

					Log.d(TAG, "Found Material Lib: " + materialLibPath);
					if(mFile != null)
						matLib.parse(materialLibPath, null, null);
					else
						matLib.parse(materialLibPath, mResources.getResourceTypeName(mResourceId), mResources.getResourcePackageName(mResourceId));
				} else if(type.equals(USE_MATERIAL)) {
					currentMaterialName = parts.nextToken();
					if(currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D());
						//Log.i("Parsing object: " + currObjIndexData.targetObj.getName());
						//addChildSetParent(currentGroup, currObjIndexData.targetObj);
						currentObjHasFaces = false;
					}
					currObjIndexData.materialName = currentMaterialName;
				}
			}
			buffer.close();

			if(currentObjHasFaces) {
				//Log.i("Parsing object: " + currObjIndexData.targetObj.getName());
				objIndices.add(currObjIndexData);
			}


		} catch (IOException e) {
			throw new ParsingException(e);
		}

		int numObjects = objIndices.size();

		for(int j=0; j<numObjects; ++j) {
			ObjIndexData oid = objIndices.get(j);

			int i;
			float[] aVertices 	= new float[oid.vertexIndices.size() * 3];
			float[] aTexCoords 	= new float[oid.texCoordIndices.size() * 2];
			float[] aNormals 	= new float[oid.normalIndices.size() * 3];
			short[] aIndices 		= new short[oid.vertexIndices.size()];

			for(i=0; i<oid.vertexIndices.size(); ++i) {
				int faceIndex = oid.vertexIndices.get(i) * 3;
				int vertexIndex = i * 3;
				try {
					aVertices[vertexIndex] = vertices.get(faceIndex);
					aVertices[vertexIndex+1] = vertices.get(faceIndex + 1);
					aVertices[vertexIndex+2] = vertices.get(faceIndex + 2);
					aIndices[i] = (short)i;
				} catch(ArrayIndexOutOfBoundsException e) {
					Log.d(TAG, "Obj array index out of bounds: " + vertexIndex + ", " + faceIndex);
				}
			}
			if(texCoords != null && texCoords.size() > 0) {
				for(i=0; i<oid.texCoordIndices.size(); ++i) {
					int texCoordIndex = oid.texCoordIndices.get(i) * 2;
					int ti = i * 2;
					aTexCoords[ti] = texCoords.get(texCoordIndex);
					aTexCoords[ti + 1] = texCoords.get(texCoordIndex + 1);
				}
			}
			for(i=0; i<oid.colorIndices.size(); ++i) {
				int colorIndex = oid.colorIndices.get(i) * 4;
				int ti = i * 4;
				aTexCoords[ti] = texCoords.get(colorIndex);
				aTexCoords[ti + 1] = texCoords.get(colorIndex + 1);
				aTexCoords[ti + 2] = texCoords.get(colorIndex + 2);
				aTexCoords[ti + 3] = texCoords.get(colorIndex + 3);
			}
			for(i=0; i<oid.normalIndices.size(); ++i){
				int normalIndex = oid.normalIndices.get(i) * 3;
				int ni = i * 3;
				if(normals.size() == 0) {
					Log.e(TAG, "["+getClass().getName()+"] There are no normals specified for this model. Please re-export with normals.");
					throw new ParsingException("["+getClass().getName()+"] There are no normals specified for this model. Please re-export with normals.");
				}
				aNormals[ni] = normals.get(normalIndex);
				aNormals[ni+1] = normals.get(normalIndex + 1);
				aNormals[ni+2] = normals.get(normalIndex + 2);
			}

			oid.targetObj.setData(aVertices, aTexCoords, aIndices);
			matLib.setMaterial(oid.targetObj, oid.materialName);

            parsedObjects.add(oid.targetObj);

		}
		/*for(Object3D group : groups.values()) {
			if(group.getParent()==null)
				addChildSetParent(mRootObject, group);
		}*/

		/*if(mRootObject.getNumChildren() == 1 && !mRootObject.getChildAt(0).isContainer())
			mRootObject = mRootObject.getChildAt(0);*/

		return parsedObjects;
	}

	private static String generateObjectName() {
		return "Object" + (int) (Math.random() * 10000);
	}

	protected class ObjIndexData {
		public Object3D targetObj;

		public ArrayList<Integer> vertexIndices;
		public ArrayList<Integer> texCoordIndices;
		public ArrayList<Integer> colorIndices;
		public ArrayList<Integer> normalIndices;

		public String materialName;

		public ObjIndexData(Object3D targetObj) {
			this.targetObj = targetObj;
			vertexIndices = new ArrayList<Integer>();
			texCoordIndices = new ArrayList<Integer>();
			colorIndices = new ArrayList<Integer>();
			normalIndices = new ArrayList<Integer>();
		}
	}

	protected class MaterialLib {
		private final String MATERIAL_NAME = "newmtl";
		private final String AMBIENT_COLOR = "Ka";
		private final String DIFFUSE_COLOR = "Kd";
		private final String SPECULAR_COLOR = "Ks";
		private final String SPECULAR_COEFFICIENT = "Ns";
		private final String ALPHA_1 = "d";
		private final String ALPHA_2 = "Tr";
		private final String AMBIENT_TEXTURE = "map_Ka";
		private final String DIFFUSE_TEXTURE = "map_Kd";
		private final String SPECULAR_COLOR_TEXTURE = "map_Ks";
		private final String SPECULAR_HIGHLIGHT_TEXTURE = "map_Ns";
		private final String ALPHA_TEXTURE_1 = "map_d";
		private final String ALPHA_TEXTURE_2 = "map_Tr";
		private final String BUMP_TEXTURE = "map_Bump";

		private Stack<MaterialDef> mMaterials;
		private String mResourcePackage;

		public MaterialLib() {
			mMaterials = new Stack<LoaderOBJ.MaterialDef>();
		}

		public void parse(String materialLibPath, String resourceType, String resourcePackage) {
			BufferedReader buffer = null;
			if(mFile == null) {
				mResourcePackage = resourcePackage;
				int identifier = mResources.getIdentifier(materialLibPath, resourceType, resourcePackage);
				try {
					InputStream fileIn = mResources.openRawResource(identifier);
					buffer = new BufferedReader(new InputStreamReader(fileIn));
				} catch(Exception e) {
					Log.e(TAG, "["+getClass().getCanonicalName()+"] Could not find material library file (.mtl).");
					return;
				}
			} else {
				try {
					File materialFile = new File(mFile.getParent() + File.separatorChar + materialLibPath);
					buffer = new BufferedReader(new FileReader(materialFile));
				} catch (Exception e) {
					Log.e(TAG, "["+getClass().getCanonicalName()+"] Could not find file.");
					e.printStackTrace();
					return;
				}
			}

			String line;
			MaterialDef matDef = null;

			try {
				while((line = buffer.readLine()) != null) {
					// Skip comments and empty lines.
					if(line.length() == 0 || line.charAt(0) == '#')
						continue;
					StringTokenizer parts = new StringTokenizer(line, " ");
					int numTokens = parts.countTokens();

					if(numTokens == 0)
						continue;
					String type = parts.nextToken();
					type = type.replaceAll("\\t", "");
					type = type.replaceAll(" ", "");

					if(type.equals(MATERIAL_NAME)) {
						if(matDef != null) mMaterials.add(matDef);
						matDef = new MaterialDef();
						matDef.name = parts.hasMoreTokens() ? parts.nextToken() : "";
						Log.d(TAG, "Parsing material: " + matDef.name);
					} else if(type.equals(DIFFUSE_COLOR)) {
						matDef.diffuseColor = getColorFromParts(parts);
					} else if(type.equals(AMBIENT_COLOR)) {
						matDef.ambientColor = getColorFromParts(parts);
					} else if(type.equals(SPECULAR_COLOR)) {
						matDef.specularColor = getColorFromParts(parts);
					} else if(type.equals(SPECULAR_COEFFICIENT)) {
						matDef.specularCoefficient = Float.parseFloat(parts.nextToken());
					} else if(type.equals(ALPHA_1) || type.equals(ALPHA_2)) {
						matDef.alpha = Float.parseFloat(parts.nextToken());
					} else if(type.equals(AMBIENT_TEXTURE)) {
						matDef.ambientTexture = parts.nextToken();
					} else if(type.equals(DIFFUSE_TEXTURE)) {
						matDef.diffuseTexture = parts.nextToken();
					} else if(type.equals(SPECULAR_COLOR_TEXTURE)) {
						matDef.specularColorTexture = parts.nextToken();
					} else if(type.equals(SPECULAR_HIGHLIGHT_TEXTURE)) {
						matDef.specularHighlightTexture = parts.nextToken();
					} else if(type.equals(ALPHA_TEXTURE_1) || type.equals(ALPHA_TEXTURE_2)) {
						matDef.alphaTexture = parts.nextToken();
					} else if(type.equals(BUMP_TEXTURE)) {
						matDef.bumpTexture = parts.nextToken();
					}
				}
				if(matDef != null) mMaterials.add(matDef);
				buffer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

        public void setMaterial(Object3D object, String materialName){
            MaterialDef matDef = null;
            for(int i=0; i<mMaterials.size(); ++i) {
				if(mMaterials.get(i).name.equals(materialName))
				{
					matDef = mMaterials.get(i);
					break;
				}
			}
            if(matDef != null){
                if(matDef.diffuseTexture != null) {
                    Log.d(TAG, "Material Texture " + matDef.diffuseTexture);
                    if (mFile == null) {
                        final String fileNameWithoutExtension = getFileNameWithoutExtension(matDef.diffuseTexture);
                        int id = mResources.getIdentifier(fileNameWithoutExtension, "drawable", mResourcePackage);
                        int etc1Id = mResources.getIdentifier(fileNameWithoutExtension, "raw", mResourcePackage);
                        if (etc1Id != 0) {
                            object.setTextureId(etc1Id);
                        } else if (id != 0) {
                            Log.d(TAG, "Material drawable " + matDef.diffuseTexture);
                            object.setTextureId(id);
                        }
                    } else {
                        String filePath = mFile.getParent() + File.separatorChar + getOnlyFileName(matDef.diffuseTexture);
                        object.setTexturePath(filePath);
                    }
                }
                object.setColor(matDef.diffuseColor);
            }else{
                Log.w(TAG, "Null material");
            }
        }

		private int getColorFromParts(StringTokenizer parts) {
			int r = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int g = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int b = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			return Color.rgb(r, g, b);
		}
	}

	protected class MaterialDef {

		public String name;
		public int ambientColor;
		public int diffuseColor;
		public int specularColor;
		public float specularCoefficient;
		public float alpha = 1f;
		public String ambientTexture;
		public String diffuseTexture;
		public String specularColorTexture;
		public String specularHighlightTexture;
		public String alphaTexture;
		public String bumpTexture;
	}

    protected String getOnlyFileName(String fileName) {
		String fName = new String(fileName);
		int indexOf = fName.lastIndexOf("\\");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
	}

    protected String getFileNameWithoutExtension(String fileName) {
        String fName = fileName.substring(0, fileName.lastIndexOf("."));
        int indexOf = fName.lastIndexOf("\\");
        if (indexOf > -1)
            fName = fName.substring(indexOf + 1, fName.length());
        indexOf = fName.lastIndexOf("/");
        if (indexOf > -1)
            fName = fName.substring(indexOf + 1, fName.length());
        return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
    }
}
