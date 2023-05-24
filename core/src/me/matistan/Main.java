package me.matistan;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main extends ApplicationAdapter implements InputProcessor {
    public static final int SCREEN_WIDTH = 1728;
    public static final int SCREEN_HEIGHT = 972;
    SpriteBatch batch;
    BitmapFont font;
    int  cx, cy, sx, sy, mouseX, mouseY, clickedX, clickedY, generation, neighbors;
    double time, delta, zoom;
    boolean isButtonPreviouslyClicked, playing;
    Texture start, next, restart, textField, load, zoomIn, zoomOut, delete, export;
    TextureRegion live, selected;
    List<Point> startCells, cells, tempCells;
    TextField loadField;
    Skin skin;
    Stage stage;
    InputMultiplexer inputMultiplexer;

    @Override
    public void create () {
        startCells = new LinkedList<>();
        cells = new LinkedList<>();
        tempCells = new LinkedList<>();
        batch = new SpriteBatch();
        font = new BitmapFont();
        zoom = 128;
        cx = 0;
        cy = 0;
        sx = 0;
        sy = 0;
        time = 0;
        delta = 0.3;
        generation = 0;
        playing = false;
        neighbors = 0;
        isButtonPreviouslyClicked = false;
        clickedX = 0;
        clickedY = 0;
        stage = new Stage();
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        loadField = new TextField("", skin);
        loadField.setSize(300, 50);
        loadField.setPosition(600, 25);
        stage.addActor(loadField);
        start = new Texture("start.png");
        next = new Texture("next.png");
        restart = new Texture("restart.png");
        zoomIn = new Texture("ZoomIn.png");
        zoomOut = new Texture("zoomOut.png");
        delete = new Texture("delete.png");
        load = new Texture("load.png");
        export = new Texture("export.png");
        textField = new Texture("textField.png");
    }

    @Override
    public void render () {
        selected = new TextureRegion(new Texture("selected.png"), (int) zoom, (int) zoom);
        live = new TextureRegion(new Texture("live.png"), (int) zoom, (int) zoom);
        mouseX = Gdx.input.getX();
        mouseY = SCREEN_HEIGHT - Gdx.input.getY() - 1;
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            clickedX = mouseX;
            clickedY = mouseY;
        }
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        font.draw(batch, "X: " + (cx - realMod(cx, (int) zoom)) / zoom + " Y: " + (cy - realMod(cy, (int) zoom)) / zoom, 0, SCREEN_HEIGHT - 5);
        font.draw(batch, "zoom: "+zoom, 0, SCREEN_HEIGHT - 5 - 4*font.getCapHeight());
        font.draw(batch, "generation: " + generation, 0, SCREEN_HEIGHT - 5 - 2*font.getCapHeight());
        font.draw(batch, "mouse X: " + mouseX+" mouse Y: "+mouseY, 0, SCREEN_HEIGHT - 5 - 6*font.getCapHeight());
        if(playing) {
            time += delta;
            if((time - delta) % 1 > time % 1) {
                generation += 1;
                nextStep();
            }
        }
        if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || (mouseX < 1100 && mouseY < 100)) {
            if(mouseX != sx || mouseY != sy) {
                cx += mouseX - sx;
                cy += mouseY - sy;
            }
        }
        for(Point p:cells) {
            batch.draw(live, (float) (p.getX() * zoom + (mouseX - cx)), (float) (p.getY() * zoom + (mouseY - cy)));
        }
        sx = mouseX;
        sy = mouseY;
        if((mouseX >= 1100 || mouseY >= 100) && generation == 0) {
            batch.draw(selected, (cx - realMod(cx, zoom)) + mouseX - cx, (cy - realMod(cy, zoom)) + mouseY - cy);
            if(isButtonPreviouslyClicked && !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && clickedX == mouseX && clickedY == mouseY) {
                if(startCells.contains(new Point((int) ((cx - realMod(cx, zoom)) / zoom), (int) ((cy - realMod(cy, zoom)) / zoom)))) {
                    startCells.remove(new Point((int) ((cx - realMod(cx, zoom)) / zoom), (int) ((cy - realMod(cy, zoom)) / zoom)));
                    cells.remove(new Point((int) ((cx - realMod(cx, zoom)) / zoom), (int) ((cy - realMod(cy, zoom)) / zoom)));
                } else {
                    startCells.add(new Point((int) ((cx - realMod(cx, zoom)) / zoom), (int) ((cy - realMod(cy, zoom)) / zoom)));
                    cells.add(new Point((int) ((cx - realMod(cx, zoom)) / zoom), (int) ((cy - realMod(cy, zoom)) / zoom)));
                }
            }
        }
        if(mouseX < 100 && mouseY < 100) {
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                playing = !playing;
            }
            if(playing) {
                start = new Texture("stopClicked.png");
            } else {
                start = new Texture("startClicked.png");
            }
        } else {
            if(playing) {
                start = new Texture("stop.png");
            } else {
                start = new Texture("start.png");
            }
        }
        batch.draw(start, 0, 0);
        if(mouseX < 200 && mouseY < 100 && mouseX >= 100) {
            next = new Texture("nextClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                generation += 1;
                nextStep();
            }
        } else {
            next = new Texture("next.png");
        }
        batch.draw(next, 100, 0);
        if(generation == 0) {
            restart = new Texture("restartBlocked.png");
        } else if(mouseX < 300 && mouseY < 100 && mouseX >= 200) {
            restart = new Texture("restartClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                generation = 0;
                cells.clear();
                cells.addAll(startCells);
            }
        } else {
            restart = new Texture("restart.png");
        }
        batch.draw(restart, 200, 0);
        if(zoom >= 512) {
            zoomIn = new Texture("ZoomInBlocked.png");
        } else if(mouseX < 400 && mouseY < 100 && mouseX >= 300) {
            zoomIn = new Texture("ZoomInClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                zoom *= 2;
                cx = cx - mouseX + SCREEN_WIDTH / 2;
                cy = cy - mouseY + SCREEN_HEIGHT / 2;
                cx *= 2;
                cy *= 2;
                cx = cx + mouseX - SCREEN_WIDTH / 2;
                cy = cy + mouseY - SCREEN_HEIGHT / 2;
            }
        } else {
            zoomIn = new Texture("ZoomIn.png");
        }
        batch.draw(zoomIn, 300, 0);
        if(zoom <= 1) {
            zoomOut = new Texture("zoomOutBlocked.png");
        } else if(mouseX < 500 && mouseY < 100 && mouseX >= 400) {
            zoomOut = new Texture("zoomOutClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                zoom /= 2;
                cx = cx - mouseX + SCREEN_WIDTH / 2;
                cy = cy - mouseY + SCREEN_HEIGHT / 2;
                cx /= 2;
                cy /= 2;
                cx = cx + mouseX - SCREEN_WIDTH / 2;
                cy = cy + mouseY - SCREEN_HEIGHT / 2;
            }
        } else {
            zoomOut = new Texture("zoomOut.png");
        }
        batch.draw(zoomOut, 400, 0);
        if(generation != 0 || startCells.size() == 0 || playing) {
            delete = new Texture("deleteBlocked.png");
        } else if(mouseX < 600 && mouseY < 100 && mouseX >= 500) {
            delete = new Texture("deleteClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                startCells.clear();
                cells.clear();
            }
        } else {
            delete = new Texture("delete.png");
        }
        batch.draw(delete, 500, 0);
        if(playing || generation != 0) {
            load = new Texture("loadBlocked.png");
        } else if(mouseX < 1000 && mouseY < 100 && mouseX >= 900) {
            load = new Texture("loadClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                File dane = new File(loadField.getText());
                try {
                    if(dane.exists()) {
                        startCells.clear();
                        cells.clear();
                        Scanner scanner = new Scanner(dane);
                        while(scanner.hasNext()) {
                            Point point = new Point(scanner.nextInt(), scanner.nextInt());
                            startCells.add(point);
                            cells.add(point);
                        }
                    }
                } catch (Exception ignored) {}
            }
        } else {
            load = new Texture("load.png");
        }
        batch.draw(load, 900, 0);
        if(cells.size() == 0) {
            export = new Texture("exportBlocked.png");
        } else if(mouseX < 1100 && mouseY < 100 && mouseX >= 1000) {
            export = new Texture("exportClicked.png");
            if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                String path = loadField.getText();
                File dane = new File(path);
                if(dane.exists()) {
                    try{
                        FileWriter fw = new FileWriter(dane);
                        for(Point p:cells) {
                            fw.write((int)p.getX()+" "+(int)p.getY()+"\n");
                        }
                        fw.close();
                    } catch (Exception ignored) {}
                }
            }
        } else {
            export = new Texture("export.png");
        }
        batch.draw(export, 1000, 0);
        batch.draw(textField, 600, 0);
        batch.end();
        stage.draw();
        isButtonPreviouslyClicked = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    public void nextStep() {
        tempCells.clear();
        neighbors = 0;
        for(Point p: cells) {
            for(int i = -1; i < 2; i++) {
                for(int j = -1; j < 2; j++) {
                    if(i == 0 && j == 0) {
                        neighbors = 0;
                        for(int k = -1; k < 2; k++) {
                            for(int l = -1; l < 2; l++) {
                                if((k != 0 || l != 0) && cells.contains(new Point((int) (p.getX() + i + k), (int) (p.getY() + j + l)))) {
                                    neighbors += 1;
                                }
                            }
                        }
                        if(neighbors >= 2 && neighbors <= 3) {
                            if(!tempCells.contains(new Point((int) (p.getX() + i), (int) (p.getY() + j)))) {
                                tempCells.add(new Point(new Point((int) (p.getX() + i), (int) (p.getY() + j))));
                            }
                        }
                    } else if(!cells.contains(new Point((int) (p.getX() + i), (int) (p.getY() + j)))) {
                        neighbors = 0;
                        for(int k = -1; k < 2; k++) {
                            for (int l = -1; l < 2; l++) {
                                if((k != 0 || l != 0) && cells.contains(new Point((int) (p.getX() + i + k), (int) (p.getY() + j + l)))) {
                                    neighbors += 1;
                                }
                            }
                        }
                        if(neighbors == 3) {
                            if(!tempCells.contains(new Point((int) (p.getX() + i), (int) (p.getY() + j)))) {
                                tempCells.add(new Point(new Point((int) (p.getX() + i), (int) (p.getY() + j))));
                            }
                        }
                    }
                }
            }
        }
        cells.clear();
        cells.addAll(tempCells);
        tempCells.clear();
    }

    float realMod(float a, double b) {
        if(b == 0) {
            return 0;
        }
        if(a >= 0) {
            while(a - b >=0) {
                a -= b;
            }
            return a;
        } else if(a < 0) {
            while(a <0) {
                a += b;
            }
            return a;
        } else {
            return 0;
        }
    }

    @Override
    public void dispose () {
        batch.dispose();
        start.dispose();
        next.dispose();
        skin.dispose();
        restart.dispose();
        zoomIn.dispose();
        zoomOut.dispose();
        font.dispose();
        delete.dispose();
        load.dispose();
        export.dispose();
        textField.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            cx += amountY * 128;
        } else if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            if(amountY < 0) {
                if(zoom <= 256) {
                    zoom *= 2;
                    cx *= 2;
                    cy *= 2;
                }
            } else {
                if(zoom >= 2) {
                    zoom /= 2;
                    cx /= 2;
                    cy /= 2;
                }
            }
        } else {
            cy -= amountY * 128;
        }
        return false;
    }
}