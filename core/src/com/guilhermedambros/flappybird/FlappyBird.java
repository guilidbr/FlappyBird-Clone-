package com.guilhermedambros.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture[] passaro;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private float variacao = 0;
	private float variacaoCano = 0;
	private Random numeroRandomico;
	private BitmapFont fonte;
	private BitmapFont msgReiniciar;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoAlto;
	private Rectangle retanguloCanoBaixo;
	//private ShapeRenderer shapeRenderer;

	//Atributos de configuração
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float velocidadeQueda = 0;
	private float posicaoInicialPassaroVertical;
	private float posicaoMovimentoCanoH;
	private float distanciaCanos;
	private float deltaTime;
	private float distanciaCanosRandomica;
	private int estadoJogo = 0; //0->Não iniciado || 1->Iniciado || 2 -> Game over
	private int pontuacao = 0;
	private boolean marcouPonto;

	//Camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;


	
	@Override
	public void create () {
		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().scale(7);

		msgReiniciar = new BitmapFont();
		msgReiniciar.setColor(Color.WHITE);
		msgReiniciar.getData().setScale(3);
		numeroRandomico = new Random();
		batch = new SpriteBatch();
		fundo = new Texture("fundo.png");
		passaro = new Texture[3];
		passaro[0] = new Texture("passaro1.png");
		passaro[1] = new Texture("passaro2.png");
		passaro[2] = new Texture("passaro3.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		circuloPassaro = new Circle();
		//shapeRenderer = new ShapeRenderer();

		//CONFIGURAÇÃO DA CAMERA
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialPassaroVertical = alturaDispositivo / 2;
		posicaoMovimentoCanoH = larguraDispositivo - canoTopo.getWidth();
		distanciaCanos = 350;
	}

	@Override
	public void render () {
		camera.update();
		//Limpar framse anteriores, otimização
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		deltaTime = Gdx.graphics.getDeltaTime();
		variacao += deltaTime * 10;

		if (variacao > 2) variacao = 0;

		if (estadoJogo == 0){
			if (Gdx.input.justTouched()){
				estadoJogo = 1;
			}
		}else {
			velocidadeQueda++;
			if (posicaoInicialPassaroVertical > 0 || velocidadeQueda < 0) {
				posicaoInicialPassaroVertical = posicaoInicialPassaroVertical - velocidadeQueda;
			}

			if(estadoJogo == 1){
				posicaoMovimentoCanoH -= deltaTime * 250;
				variacaoCano -= Gdx.graphics.getDeltaTime() * 10;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -15;
				}

				//verifica se o cano atingiu o fim da tela
				if (posicaoMovimentoCanoH < (0 - canoBaixo.getWidth())) {
					posicaoMovimentoCanoH = larguraDispositivo;
					distanciaCanosRandomica = numeroRandomico.nextInt(400) - 200;
					marcouPonto = false;
				}

				//verifica se o cano passou o fim da tela
				if (posicaoMovimentoCanoH < 120 - canoBaixo.getWidth()){
					if (!marcouPonto) {
						pontuacao++;
						marcouPonto = true;
					}
				}
			}else{
				if (Gdx.input.justTouched()) {
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialPassaroVertical = alturaDispositivo / 2;
					posicaoMovimentoCanoH = larguraDispositivo - canoTopo.getWidth();
				}
			}


		}

		//Configurando dados de projeção da camera
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(canoTopo, posicaoMovimentoCanoH, alturaDispositivo/2 + distanciaCanos/2 +distanciaCanosRandomica);
		batch.draw(canoBaixo, posicaoMovimentoCanoH, (alturaDispositivo/2 - canoBaixo.getHeight()) - distanciaCanos/2 +distanciaCanosRandomica);
		batch.draw(passaro[(int)variacao], 120, posicaoInicialPassaroVertical);
		fonte.draw(batch, String.valueOf(pontuacao),larguraDispositivo/2, alturaDispositivo-50);
		if (estadoJogo == 2) {//Se game over
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			msgReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo/2 - 200, alturaDispositivo/2 - gameOver.getHeight()/2);

		}
		batch.end();

		circuloPassaro.set(120 + passaro[0].getWidth() / 2, posicaoInicialPassaroVertical + passaro[0].getHeight() / 2, passaro[0].getWidth()/2);
		retanguloCanoBaixo = new Rectangle(
				posicaoMovimentoCanoH,
				(alturaDispositivo/2 - canoBaixo.getHeight()) - distanciaCanos/2 +distanciaCanosRandomica,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()
		);
		retanguloCanoAlto = new Rectangle(
				posicaoMovimentoCanoH,
				alturaDispositivo/2 + distanciaCanos/2 +distanciaCanosRandomica,
				canoTopo.getWidth(),
				canoBaixo.getHeight()
		);
		//desenhar formas
		/*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		shapeRenderer.end();*/

		//testa colisão
		if (Intersector.overlaps(circuloPassaro, retanguloCanoAlto) || Intersector.overlaps(circuloPassaro, retanguloCanoBaixo)
				|| (posicaoInicialPassaroVertical <= 0) || (posicaoInicialPassaroVertical >= alturaDispositivo) ){
			//colidiu
			estadoJogo = 2;
		}

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
