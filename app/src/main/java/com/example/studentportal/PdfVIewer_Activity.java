package com.example.studentportal;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class PdfVIewer_Activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        WebView webView = findViewById(R.id.webView);

        // Enable JavaScript for the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set a WebViewClient to open links within the WebView
        webView.setWebViewClient(new WebViewClient());

//         Load the PDF using Google Docs
        String pdfUrl = "https://docs.google.com/document/d/1kWOKlGp7tLYyzc58wAmDAMFfjQyWaae0DA3N3KuGh4w/edit?usp=sharing";




        webView.loadUrl(pdfUrl);

    }
}