/** public class TileRenderer {
    private SpriteBatch batch;
    private Texture grassTop;
    private Texture dirt;
    private Texture stone;
    private Texture platformLeft, platformMiddle, platformRight;

    public TileRenderer() {
        batch = new SpriteBatch();
        grassTop = new Texture("ui/Tile_Grass_Top.png");
        dirt = new Texture("ui/Tile_Dirt.png");
        stone = new Texture("ui/Tile_Stone.png");
        platformLeft = new Texture("ui/Tile_Platform_Left.png");
        platformMiddle = new Texture("ui/Tile_Platform_Middle.png");
        platformRight = new Texture("ui/Tile_Platform_Right.png");
    }

    public void render(List<TileInfo> tiles) {
        batch.begin();
        for (TileInfo info : tiles) {
            switch (info.type) {
                case GRASS_TOP:
                    batch.draw(grassTop, info.x, info.y);
                    break;
                case DIRT:
                    batch.draw(dirt, info.x, info.y);
                    break;
                case STONE:
                    batch.draw(stone, info.x, info.y);
                    break;
                case PLATFORM_LEFT:
                    batch.draw(platformLeft, info.x, info.y);
                    break;
                case PLATFORM_MIDDLE:
                    batch.draw(platformMiddle, info.x, info.y);
                    break;
                case PLATFORM_RIGHT:
                    batch.draw(platformRight, info.x, info.y);
                    break;
            }
        }
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        grassTop.dispose();
        dirt.dispose();
        stone.dispose();
        platformLeft.dispose();
        platformMiddle.dispose();
        platformRight.dispose();
    }
}
*/
