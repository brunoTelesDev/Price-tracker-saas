package com.price_tracker.saas.integration.amazon;

import com.price_tracker.saas.integration.PriceProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AmazonProvider implements PriceProvider {

    @Override
    public BigDecimal buscarPreco(String url) {

        try {

            System.out.println("👻 [JSoUP/AMAZON] Vasculhando: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Sec-Ch-Ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                    .header("Sec-Ch-Ua-Mobile", "?0")
                    .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .referrer("https://www.google.com/")
                    .followRedirects(true)
                    .timeout(15000)
                    .get();

            System.out.println("📄 Título da página: " + doc.title());

            // 1. SEO
            Element tagPreco = doc.selectFirst(
                    "[itemprop='price'], meta[property='product:price:amount']"
            );

            if (tagPreco != null) {

                String valor = tagPreco.hasAttr("content")
                        ? tagPreco.attr("content")
                        : tagPreco.text();

                if (valor != null && !valor.isEmpty()) {

                    System.out.println("🎯 [SEO] Preço rastreado: " + valor);

                    return formatarPreco(valor);
                }
            }

            // 2. HTML
            Element spanPreco = doc.selectFirst(
                    ".andes-money-amount__fraction, " +
                            ".a-price-whole, " +
                            "span[class*='price']"
            );

            if (spanPreco != null) {

                System.out.println(
                        "🔍 [HTML] Preço visual encontrado: '"
                                + spanPreco.text()
                                + "'"
                );

                return formatarPreco(spanPreco.text());
            }

            // 3. JSON / SCRIPTS
            String htmlCompleto = doc.html();

            Pattern patternJSON = Pattern.compile(
                    "\"(?:price|amount)\"\\s*:\\s*\"?([0-9]{2,7}(?:[.,][0-9]{1,2})?)\"?"
            );

            Matcher matcherJSON = patternJSON.matcher(htmlCompleto);

            while (matcherJSON.find()) {

                String precoOculto = matcherJSON.group(1);

                BigDecimal precoCalculado =
                        formatarPreco(precoOculto);

                if (precoCalculado.compareTo(new BigDecimal("10")) > 0) {

                    System.out.println(
                            "🥷 [NINJA] Preço achado no script: "
                                    + precoOculto
                    );

                    return precoCalculado;
                }
            }

            // 4. R$
            Pattern patternRS = Pattern.compile(
                    "R\\$\\s*([0-9]{1,3}(?:\\.[0-9]{3})*(?:,[0-9]{2})?)"
            );

            Matcher matcherRS = patternRS.matcher(doc.text());

            if (matcherRS.find()) {

                String precoRS = matcherRS.group(1);

                System.out.println(
                        "🪓 [BRUTO] Preço encontrado: R$ "
                                + precoRS
                );

                return formatarPreco(precoRS);
            }

            System.out.println(
                    "⚠️ Preço não encontrado no HTML de: " + url
            );

            return BigDecimal.ZERO;

        } catch (Exception e) {

            System.out.println(
                    "❌ Erro no Jsoup: "
                            + e.getMessage()
            );

            return BigDecimal.ZERO;
        }
    }

    private BigDecimal formatarPreco(String precoTexto) {

        String semPontos =
                precoTexto.replace(".", "");

        String comPontoDecimal =
                semPontos.replace(",", ".");

        String limpo =
                comPontoDecimal.replaceAll(
                        "[^0-9.]",
                        ""
                );

        if (limpo.contains(".")) {

            String[] partes =
                    limpo.split("\\.");

            limpo = partes[0];
        }

        if (limpo.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal precoFinal =
                new BigDecimal(limpo);

        System.out.println(
                "💰 Preço convertido com sucesso: R$ "
                        + precoFinal
        );

        return precoFinal;
    }
}